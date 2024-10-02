package org.ai.toolkit.aitk.llamacpp;

import ai.djl.util.Platform;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import static org.ai.toolkit.aitk.llamacpp.Constants.DOWNLOAD_URL;
import static org.ai.toolkit.aitk.llamacpp.Constants.NATIVE_FILE_MAP;

public class DownLoadOllama {
    public static void main(String[] args) throws Exception {
        Platform platform = Platform.fromSystem("ollama");
        String fileNameKey = null;
        if (platform.getOsPrefix().equals("osx")) {
            fileNameKey = platform.getOsPrefix();
        } else {
            fileNameKey = platform.getOsPrefix() + "-" + platform.getOsArch();

        }
        String fileName = NATIVE_FILE_MAP.get(fileNameKey);
        Path path = Paths.get(System.getProperty("user.dir") + "/aitk-llamacpp-engine/src/main/resources/native/" + fileName);
        if (Files.exists(path)) {
            return;
        }
        String httpUrl = String.format(DOWNLOAD_URL, platform.getVersion(), fileName);
        URL url = new URL(httpUrl);
        try (InputStream in = url.openStream()) {
            Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

