package org.ai.toolkit.aitk.llamacpp;

import ai.djl.Model;
import ai.djl.modality.Output;
import ai.djl.translate.Translator;
import ai.djl.translate.TranslatorFactory;
import ai.djl.util.Pair;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LlamaCppTranslatorFactory implements TranslatorFactory, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Set<Pair<Type, Type>> SUPPORTED_TYPES = new HashSet<>();

    static {
    }

    @Override
    public Set<Pair<Type, Type>> getSupportedTypes() {
        return SUPPORTED_TYPES;
    }

    @Override
    public boolean isSupported(Class<?> input, Class<?> output) {
        return true;
    }

    @Override
    public <I, O> Translator<I, O> newInstance(
            Class<I> input, Class<O> output, Model model, Map<String, ?> arguments) {
        return new LlamaCppTranslator<>();
    }
}