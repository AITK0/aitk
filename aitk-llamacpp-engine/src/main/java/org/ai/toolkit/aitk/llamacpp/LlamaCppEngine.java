package org.ai.toolkit.aitk.llamacpp;

import ai.djl.Device;
import ai.djl.Model;
import ai.djl.engine.Engine;
import ai.djl.ndarray.NDManager;
import ai.djl.util.passthrough.PassthroughNDManager;

public final class LlamaCppEngine extends Engine {

    public static final String ENGINE_NAME = "LlamaCpp";
    static final int RANK = 10;
    private static final String VERSION = "3.2.1";

    private Engine alternativeEngine;
    private boolean initialized;

    private LlamaCppEngine() {

    }

    static Engine newInstance() {
        return new LlamaCppEngine();
    }

    @Override
    public Engine getAlternativeEngine() {
        if (!initialized && !Boolean.getBoolean("ai.djl.llama.disable_alternative")) {
            Engine engine = Engine.getInstance();
            if (engine.getRank() < getRank()) {
                alternativeEngine = engine;
            }
            initialized = true;
        }
        return alternativeEngine;
    }

    @Override
    public String getEngineName() {
        return ENGINE_NAME;
    }

    @Override
    public int getRank() {
        return RANK;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public boolean hasCapability(String capability) {
        return false;
    }

    @Override
    public Model newModel(String name, Device device) {
        return new LlamaCppModel(name, newBaseManager(device));
    }

    @Override
    public NDManager newBaseManager() {
        return newBaseManager(null);
    }

    @Override
    public NDManager newBaseManager(Device device) {
        return PassthroughNDManager.INSTANCE;
    }

    @Override
    public String toString() {
        return getEngineName() + ':' + getVersion() + ", " + getEngineName() + ':' + getVersion();
    }
}