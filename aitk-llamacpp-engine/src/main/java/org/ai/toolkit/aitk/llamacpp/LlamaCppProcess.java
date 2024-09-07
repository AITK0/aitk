package org.ai.toolkit.aitk.llamacpp;

import ai.djl.util.ClassLoaderUtils;
import ai.djl.util.Platform;
import ai.djl.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LlamaCppProcess {
    private static Logger LOGGER = LoggerFactory.getLogger(LlamaCppProcess.class);

    private static AtomicInteger counter = new AtomicInteger(0);

    private static final String LIB_NAME = "llama-server";

    private static final int BASE_PORT = 30000;

    private static final Map<Long, Process> ALL_PROCESS_MAP = new ConcurrentHashMap<>();

    private static final Map<String, String[]> NATIVE_FILE_MAP = new HashMap<>();

    private volatile boolean started;

    private Process process;

    private int port;

    static {
        NATIVE_FILE_MAP.put("win-x86_64", new String[]{"ggml.dll", "llama.dll", "llama-server.exe", "llava_shared.dll"});
        NATIVE_FILE_MAP.put("win-aarch64", new String[]{"libggml.dll", "libllama.dll", "libllava_shared.dll", "llama-server.exe"});
        NATIVE_FILE_MAP.put("osx-x86_64", new String[]{"llama-server"});
        NATIVE_FILE_MAP.put("osx-aarch64", new String[]{"ggml-common.h", "ggml-metal.metal", "llama-server"});
        NATIVE_FILE_MAP.put("linux-x86_64", new String[]{"llama-server"});
    }

    static {
        Thread shutdownHook = new Thread(() -> {
            ALL_PROCESS_MAP.entrySet().forEach(e -> e.getValue().destroyForcibly());
            LOGGER.info("stop all llamacpp server");
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public LlamaCppProcess() {
    }

    public void stopServer() {
        this.started = false;
        process.destroyForcibly();
    }

    public void startServer(String modelName, List<String> command) {
        try {
            int port = BASE_PORT + counter.getAndIncrement();
            this.port = port;
            Platform platform = Platform.fromSystem("llama");
            List<String> newCommand = new ArrayList<>();
            newCommand.add(loadLibraryFromClasspath(platform));
            newCommand.add("--port");
            newCommand.add(String.valueOf(port));
            newCommand.add("--host");
            newCommand.add("127.0.0.1");
            newCommand.addAll(command);
            ProcessBuilder pb = new ProcessBuilder(newCommand);
            Process process = pb.start();
            String pid = process.toString().split(", ")[0].replace("Process[pid=", "");
            modelName = modelName.substring(0, Math.min(modelName.length(), 15));
            String threadName = "W-" + pid + '-' + modelName;
            ReaderThread err =
                    new ReaderThread(threadName, process.getErrorStream(), true, this, Long.valueOf(pid));
            ReaderThread out =
                    new ReaderThread(threadName, process.getInputStream(), false, this, Long.valueOf(pid));
            err.start();
            out.start();
            ALL_PROCESS_MAP.put(Long.valueOf(pid), process);
            this.process = process;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }

    private String loadLibraryFromClasspath(Platform platform) {
        Path tmp = null;
        try {
            Path cacheFolder = Utils.getEngineCacheDir("llama");
            String version = platform.getVersion();
            String flavor = platform.getFlavor();
            String classifier = platform.getClassifier();
            Path dir = cacheFolder.resolve(version + '-' + flavor + '-' + classifier);
            LOGGER.debug("Using cache dir: {}", dir);

            Path path = dir.resolve(platform.getOsPrefix().equals("win") ? LIB_NAME + ".exe" : LIB_NAME);
            if (Files.exists(path)) {
                return path.toAbsolutePath().toString();
            }

            Files.createDirectories(cacheFolder);
            tmp = Files.createTempDirectory(cacheFolder, "tmp");

            String cuda = null;
//            if (platform.getOsPrefix().equals("win") && platform.getFlavor().startsWith("cu")) {
//                int cudaVersion = Integer.parseInt(flavor.substring(2, 5));
//                if (cudaVersion >= 122) {
//                    cuda = "cu117";
//                } else if (cudaVersion >= 117) {
//                    cuda = "cu122";
//                }
//            }
            String key = platform.getOsPrefix() + "-" + platform.getOsArch();
            if (!NATIVE_FILE_MAP.containsKey(key)) {
                throw new UnsupportedOperationException("Not supported!");
            }
            String[] files = NATIVE_FILE_MAP.get(key);
            String libPathPrefix = "native/llama-" + platform.getVersion() + "-" + platform.getOsPrefix();
            String classPath = Objects.isNull(cuda) ? libPathPrefix + "-" + platform.getOsArch() + "/" : libPathPrefix + "-" + cuda + "-" + platform.getOsArch() + "/";
            for (String file : files) {
                String libPath = classPath + file;
                LOGGER.info("Extracting {} to cache ...", libPath);
                try (InputStream is = ClassLoaderUtils.getResourceAsStream(libPath)) {
                    Files.copy(is, tmp.resolve(file), StandardCopyOption.REPLACE_EXISTING);
                }
            }

            Utils.moveQuietly(tmp, dir);
            return path.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to extract LLama native library", e);
        } finally {
            if (tmp != null) {
                Utils.deleteQuietly(tmp);
            }
        }
    }


    static final class ReaderThread extends Thread {

        private InputStream is;
        private boolean error;
        private LlamaCppProcess lifeCycle;
        private long processId;

        public ReaderThread(
                String name, InputStream is, boolean error, LlamaCppProcess lifeCycle, long processId) {
            super(name + (error ? "-stderr" : "-stdout"));
            this.is = is;
            this.error = error;
            this.lifeCycle = lifeCycle;
            this.processId = processId;
        }

        @Override
        public void run() {
            try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8)) {
                while (scanner.hasNext()) {
                    String result = scanner.nextLine();
                    if (result == null) {
                        LOGGER.warn("Got EOF: {}", getName());
                        break;
                    }
                    if (error) {
                        LOGGER.warn("{}: {}", getName(), result);
                    } else {
                        LOGGER.info("{}: {}", getName(), result);
                    }
                    if (result.contains("HTTP server listening")) {
                        lifeCycle.setStarted(true);
                    }
                }
            } catch (Exception e) {
                LOGGER.error("Couldn't create scanner - {}", getName(), e);
            } finally {
                LOGGER.info("ReaderThread({}) stopped - {}", processId, getName());
                lifeCycle.setStarted(false);
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.warn("Failed to close stream for thread - " + getName(), e);
                }
            }
        }
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public Process getProcess() {
        return process;
    }

    public void setProcess(Process process) {
        this.process = process;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
