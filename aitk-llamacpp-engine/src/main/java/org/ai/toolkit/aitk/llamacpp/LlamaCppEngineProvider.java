package org.ai.toolkit.aitk.llamacpp;

import ai.djl.engine.Engine;
import ai.djl.engine.EngineProvider;

public class LlamaCppEngineProvider implements EngineProvider {

    @Override
    public String getEngineName() {
        return LlamaCppEngine.ENGINE_NAME;
    }

    @Override
    public int getEngineRank() {
        return LlamaCppEngine.RANK;
    }

    @Override
    public Engine getEngine() {
        return InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder {
        static final Engine INSTANCE = LlamaCppEngine.newInstance();
    }
}