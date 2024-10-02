package org.ai.toolkit.aitk.llamacpp;

import ai.djl.ndarray.NDList;
import ai.djl.translate.NoBatchifyTranslator;
import ai.djl.translate.TranslatorContext;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LlamaCppTranslator<I, O> implements NoBatchifyTranslator<I, O> {

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(100000))
            .build();

    private static final String URL_FORMAT = "http://127.0.0.1:%s/api/chat";

    @Override
    public void prepare(TranslatorContext ctx) {

    }

    @Override
    public NDList processInput(TranslatorContext ctx, I input) throws Exception {
        LlamaCppModel llamaCppModel = (LlamaCppModel) ctx.getModel();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format(URL_FORMAT, llamaCppModel.getPort())))
                .POST(HttpRequest.BodyPublishers.ofString(input.toString()))
                .build();
        HttpResponse<InputStream> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
        ctx.setAttachment("out", response);
        return new NDList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public O processOutput(TranslatorContext ctx, NDList list) {
        return (O) ctx.getAttachment("out");
    }


}