package org.ai.toolkit.aitk.llamacpp;

import ai.djl.util.ClassLoaderUtils;
import ai.djl.util.Platform;
import ai.djl.util.Utils;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.ai.toolkit.aitk.llamacpp.Constants.NATIVE_FILE_MAP;

public class LlamaCppProcess {
    private static Logger LOGGER = LoggerFactory.getLogger(LlamaCppProcess.class);

    private static final Map<Long, Process> ALL_PROCESS_MAP = new ConcurrentHashMap<>();

    private volatile boolean started;

    private Process process;

    private Long pid;

    private static final Map<String, String> PLATFORM_EXE_MAP = new HashMap<>();

    static {
        PLATFORM_EXE_MAP.put("win", "ollama.exe");
        PLATFORM_EXE_MAP.put("osx", "ollama");
        PLATFORM_EXE_MAP.put("linux", "bin/ollama");

        Thread shutdownHook = new Thread(() -> {
            ALL_PROCESS_MAP.entrySet().forEach(e -> e.getValue().destroyForcibly());
            LOGGER.info("stop all llamacpp server");
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    public LlamaCppProcess() {
    }

    public void destroyForcibly() {
        this.process.destroyForcibly();
        ALL_PROCESS_MAP.remove(this.pid);
    }

    public Long runCmd(List<String> cmdList, boolean isPutMap) {
        try {
            Platform platform = Platform.fromSystem("ollama");
            List<String> newCommand = new ArrayList<>();
            newCommand.add(loadLibraryFromClasspath(platform));
            newCommand.addAll(cmdList);
            ProcessBuilder pb = new ProcessBuilder(newCommand);
            Process process = pb.start();
            String pid = process.toString().split(", ")[0].replace("Process[pid=", "");
            String threadName = "W-" + pid + "-ollama";
            ReaderThread err =
                    new ReaderThread(threadName, process.getErrorStream(), true, this, Long.valueOf(pid));
            ReaderThread out =
                    new ReaderThread(threadName, process.getInputStream(), false, this, Long.valueOf(pid));
            err.start();
            out.start();
            this.process = process;
            this.pid = Long.valueOf(pid);
            if (isPutMap) {

                ALL_PROCESS_MAP.put(this.pid, this.process);
            }
            return Long.valueOf(pid);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    public void startServer() {
        runCmd(Arrays.asList("serve"), true);
    }


    public String loadLibraryFromClasspath(Platform platform) {
        Path tmp = null;
        try {
            Path cacheFolder = Utils.getEngineCacheDir("ollama");
            String version = platform.getVersion();
            String classifier = platform.getClassifier();
            Path dir = cacheFolder.resolve(version + '-' + classifier);
            LOGGER.debug("Using cache dir: {}", dir);
            Path path = dir.resolve(PLATFORM_EXE_MAP.get(platform.getOsPrefix()));
            if (Files.exists(path)) {
                return path.toAbsolutePath().toString();
            }

            Files.createDirectories(cacheFolder);
            tmp = Files.createTempDirectory(cacheFolder, "tmp");

            String key = platform.getOsPrefix().equals("osx") ? platform.getOsPrefix() : platform.getOsPrefix() + "-" + platform.getOsArch();
            if (!NATIVE_FILE_MAP.containsKey(key)) {
                throw new UnsupportedOperationException("Not supported!");
            }
            String fileName = NATIVE_FILE_MAP.get(key);
            String filePath = "native/" + fileName;

            if (fileName.endsWith("tgz")) {
                extractTarGZ(filePath, tmp);
            } else if (fileName.endsWith("zip")) {
                extractZip(filePath, tmp, platform);
            } else {
                throw new UnsupportedOperationException("Not supported!");
            }

            Utils.moveQuietly(tmp, dir);
            /**
             * mac、linux系统给文件执行权限
             */
            if (platform.getOsPrefix().equals("osx") || platform.getOsPrefix().endsWith("linux")) {
                Set<PosixFilePermission> perms = new HashSet<>();
                perms.add(PosixFilePermission.OWNER_EXECUTE);
                Set<PosixFilePermission> currentPerms = Files.getPosixFilePermissions(path);
                currentPerms.addAll(perms);
                Files.setPosixFilePermissions(path, currentPerms);
            }
            return path.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to extract LLama native library", e);
        } finally {
            if (tmp != null) {
                Utils.deleteQuietly(tmp);
            }
        }
    }

    private void extractZip(String filePath, Path targetDir, Platform platform) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(ClassLoaderUtils.getResourceAsStream(filePath))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (platform.getOsPrefix().equals("osx") && !entry.getName().endsWith("ollama")) {
                    continue;
                }
                Path entryPath = null;
                if (platform.getOsPrefix().equals("osx")) {
                    entryPath = targetDir.resolve("ollama");
                } else {
                    entryPath = targetDir.resolve(entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    LOGGER.info("Extracting {} to cache ...", entryPath);
                    try (OutputStream out = Files.newOutputStream(entryPath)) {
                        byte[] buffer = new byte[2048];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private void extractTarGZ(String filePath, Path targetDir) throws IOException {
        GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(ClassLoaderUtils.getResourceAsStream(filePath));
        try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
            TarArchiveEntry entry;
            while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    LOGGER.info("Extracting {} to cache ...", entryPath);
                    try (OutputStream out = Files.newOutputStream(entryPath)) {
                        byte[] buffer = new byte[2048];
                        int len;
                        while ((len = tarIn.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
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

    public static void main(String[] args) throws Exception {
        Platform platform = Platform.fromSystem("ollama");
        Field osPrefix = Platform.class.getDeclaredField("osPrefix");
        osPrefix.setAccessible(true);
        osPrefix.set(platform, "linux");
        Field osArch = Platform.class.getDeclaredField("osArch");
        osArch.setAccessible(true);
        osArch.set(platform, "x86_64");

        new LlamaCppProcess().loadLibraryFromClasspath(platform);
    }
}
