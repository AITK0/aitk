package org.ai.toolkit.aitk.modelmanager;

import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.serving.wlm.Job;
import ai.djl.serving.wlm.ModelInfo;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.ai.toolkit.aitk.modelzoo.executor.InferenceCallback;
import org.ai.toolkit.aitk.modelzoo.ModelDefinition;
import org.ai.toolkit.aitk.modelzoo.executor.InferenceExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InferenceExecutorImpl implements InferenceExecutor {

    @Autowired
    private ModelManager modelManager;

    @Override
    public <P, Q> void asyncExecute(String modelId, Input input, InferenceCallback callback) {
        try {
            CompletableFuture<Q> completableFuture = execute(modelId, input);
            completableFuture.whenComplete((o, e) -> {
                if (!Objects.isNull(e)) {
                    callback.callback(e, null);
                    return;
                }
                try {
                    Output output = modelManager.getOnlineModelDefinition(modelId).postProcessAfterModel(input, o);
                    callback.callback(null, output);
                } catch (Throwable t) {
                    callback.callback(t, null);
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <P, Q> Output syncExecute(String modelId, Input input) {
        try {
            CompletableFuture<Q> completableFuture = execute(modelId, input);
            Output output = modelManager.getOnlineModelDefinition(modelId).postProcessAfterModel(input, completableFuture.join());
            return output;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private <P, Q> CompletableFuture<Q> execute(String modelId, Input input) throws Exception {
        ModelInfo modelInfo = modelManager.getOnlineModelInfo(modelId);
        ModelDefinition<P, Q> modelDefinition = modelManager.getOnlineModelDefinition(modelId);
        P preResult = modelDefinition.postProcessBeforeModel(input);
        CompletableFuture<Q> completableFuture = modelManager.getWorkLoadManager().runJob(new Job<>(modelInfo,
                preResult));
        return completableFuture;
    }

    @Override
    public <I, O> O asyncExecute(String modelId, I input, Integer modelIndex) {
        ModelInfo modelInfo = modelManager.getOnlineModelInfoList(modelId).get(modelIndex);
        CompletableFuture<O> completableFuture = modelManager.getWorkLoadManager().runJob(new Job<>(modelInfo,
                input));
        return completableFuture.join();
    }
}
