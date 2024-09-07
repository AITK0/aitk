package org.ai.toolkit.aitk.modelzoo.executor;

import ai.djl.modality.Input;
import ai.djl.modality.Output;

public interface InferenceExecutor {

    public <P, Q> void asyncExecute(String modelId, Input input, InferenceCallback callback);

    public <P, Q> Output syncExecute(String modelId, Input input);

    public <I, O> O asyncExecute(String modelId, I input, Integer modelIndex);
}
