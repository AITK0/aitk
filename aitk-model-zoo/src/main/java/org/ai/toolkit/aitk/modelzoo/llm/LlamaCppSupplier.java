package org.ai.toolkit.aitk.modelzoo.llm;

import ai.djl.ndarray.BytesSupplier;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;

public class LlamaCppSupplier implements BytesSupplier {

    private HttpResponse<InputStream> httpResponse;

    public LlamaCppSupplier(HttpResponse<InputStream> httpResponse) {
        this.httpResponse = httpResponse;
    }

    public HttpResponse<InputStream> getHttpResponse() {
        return httpResponse;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(new byte[]{});
    }
}