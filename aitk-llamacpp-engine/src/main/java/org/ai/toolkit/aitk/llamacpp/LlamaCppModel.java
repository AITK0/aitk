package org.ai.toolkit.aitk.llamacpp;

import ai.djl.BaseModel;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.DataType;
import ai.djl.nn.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;

public class LlamaCppModel extends BaseModel {
    private static Logger LOGGER = LoggerFactory.getLogger(LlamaCppModel.class);
    private static final int PORT = 11434;

    private static final String LOAD_MODEL_URL = "http://localhost:11434/api/generate";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(100000))
            .build();

    private static final String REQUEST_FORMAT = "{\"model\":\"%s\"}";

    private static final String SUCCESS = "\"done_reason\":\"load\"";

    LlamaCppModel(String name, NDManager manager) {
        super(name);
        this.manager = manager;
        this.manager.setName("llamaCppModel");
        dataType = DataType.FLOAT32;
    }

    @Override
    public void load(Path modelPath, String prefix, Map<String, ?> options) throws IOException {
        if (block != null) {
            throw new UnsupportedOperationException("Llama does not support dynamic blocks");
        }
        new LlamaCppProcess().runCmd(Arrays.asList("pull", modelName), false);
        boolean isBreak = false;
        while (true) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(LOAD_MODEL_URL))
                        .POST(HttpRequest.BodyPublishers.ofString(String.format(REQUEST_FORMAT, modelName)))
                        .build();
                HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains(SUCCESS)) {
                        isBreak = true;
                    }
                    LOGGER.info(line);
                }
                if (isBreak) {
                    break;
                }
                Thread.sleep(5000);
            } catch (Exception e) {
                LOGGER.warn("LlamaCppModel#load error", e);
            }
        }

        block = Blocks.identityBlock();
        wasLoaded = true;
    }


    @Override
    public void close() {
        if (!wasLoaded) {
            return;
        }
        LlamaCppProcess stop = new LlamaCppProcess();
        stop.runCmd(Arrays.asList("stop", modelName), false);
        super.close();
    }

    public int getPort() {
        return PORT;
    }

}