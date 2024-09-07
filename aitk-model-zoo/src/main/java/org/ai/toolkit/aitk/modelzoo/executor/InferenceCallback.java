package org.ai.toolkit.aitk.modelzoo.executor;

import ai.djl.modality.Output;

public interface InferenceCallback {

    void callback(Throwable throwable, Output output);
}
