package org.ai.toolkit.aitk.common.modelscope;

import org.ai.toolkit.aitk.common.git.GitEnum;
import org.ai.toolkit.aitk.common.git.GitUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FileDownloadUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(FileDownloadUtil.class);

    private static final long TIMEOUT = 10000L;

    private static final String DOWNLOAD_URL = "https://modelscope.cn/models/AITK/modelzoo/resolve/master";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(TIMEOUT))
            .build();
    public static void download(List<String> fileList, ConcurrentHashMap<String, DownloadState> progressMap, GitEnum gitEnum) {
        for (String file : fileList) {
            String url = file.startsWith("/") ? DOWNLOAD_URL + file : DOWNLOAD_URL + "/" + file;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "*/*")
                    .build();

            String filePath = file.startsWith("/") ? GitUtil.getModelBasePath(gitEnum) + file : GitUtil.getModelBasePath(gitEnum) + "/" + file;
            OutputStream outputStream = null;
            InputStream inputStream = null;
            try {
                HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
                long contentLength = response.headers().firstValue("Content-Length").map(Long::parseLong).orElse(-1L);
                inputStream = response.body();
                byte[] buf = new byte[20240];
                int length = 0;
                if (contentLength <= 0) {
                    progressMap.put(file, new DownloadState(new RuntimeException("File path error, unable to download")));
                    return;
                }
                String outputDirectory = filePath.substring(0, filePath.lastIndexOf("/"));
                Path outputPath = Paths.get(outputDirectory);
                Files.createDirectories(outputPath);
                String fileName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
                outputStream = Files.newOutputStream(outputPath.resolve(fileName));
                long sum = 0;
                while ((length = inputStream.read(buf)) != -1) {
                    outputStream.write(buf, 0, length);
                    sum += length;
                    int progress = (int) (sum * 1.0f / contentLength * 100);
                    progressMap.put(file, new DownloadState(progress));
                }
                outputStream.flush();
                progressMap.put(file, new DownloadState(100));
            } catch (Exception e) {
                progressMap.put(file, new DownloadState(e));
                LOGGER.error("FileDownloadUtil#download", e);
            } finally {
                close(inputStream, outputStream);
            }
        }
    }

    private static void close(InputStream inputStream, OutputStream outputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            LOGGER.warn("FileDownloadUtil#close", e);
        }
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Exception e) {
            LOGGER.warn("FileDownloadUtil#close", e);
        }
    }
}
