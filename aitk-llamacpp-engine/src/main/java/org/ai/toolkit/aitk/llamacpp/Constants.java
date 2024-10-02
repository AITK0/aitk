package org.ai.toolkit.aitk.llamacpp;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    public static final String DOWNLOAD_URL = "https://github.com/ollama/ollama/releases/download/v%s/%s";
    public static final Map<String, String> NATIVE_FILE_MAP = new HashMap<>();

    static {
        NATIVE_FILE_MAP.put("win-x86_64", "ollama-windows-amd64.zip");
        NATIVE_FILE_MAP.put("win-aarch64", "ollama-windows-arm64.zip");
        NATIVE_FILE_MAP.put("osx", "Ollama-darwin.zip");
        NATIVE_FILE_MAP.put("linux-x86_64", "ollama-linux-amd64.tgz");
        NATIVE_FILE_MAP.put("linux-aarch64", "ollama-linux-arm64.tgz");
    }
}
