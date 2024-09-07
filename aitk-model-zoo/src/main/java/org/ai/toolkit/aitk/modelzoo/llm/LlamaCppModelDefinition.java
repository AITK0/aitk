package org.ai.toolkit.aitk.modelzoo.llm;

import ai.djl.modality.Input;
import ai.djl.modality.Output;
import ai.djl.repository.zoo.Criteria;
import org.ai.toolkit.aitk.llamacpp.LlamaCppTranslatorFactory;
import org.ai.toolkit.aitk.modelzoo.AbstractBaseModelDefinition;
import org.ai.toolkit.aitk.modelzoo.bean.ModelBasicInfo;
import org.ai.toolkit.aitk.modelzoo.bean.Param;
import org.ai.toolkit.aitk.modelzoo.constant.EngineEnum;
import org.ai.toolkit.aitk.modelzoo.constant.FileExtension;
import org.ai.toolkit.aitk.modelzoo.constant.ModelTypeEnum;

import java.io.InputStream;
import java.net.http.HttpResponse;
import java.util.*;

public class LlamaCppModelDefinition extends AbstractBaseModelDefinition<String, HttpResponse<InputStream>> {

    private final String modelName;

    private final String size;

    private final String modelPath;

    private final ModelBasicInfo modelBasicInfo;

    private static final String FORMAT = "llm/%s/%s";

    @Override
    public String getId() {
        return String.format(FORMAT, modelName, size);
    }

    @Override
    public List<String> getModelFileList() {
        return Arrays.asList(modelPath);
    }

    private List<Param> inferenceParams = new ArrayList<>();

    private List<Param> modelParams = new ArrayList<>();

    public LlamaCppModelDefinition(String modelName, String size, String modelPath, ModelBasicInfo modelBasicInfo) {
        this.modelName = modelName;
        this.size = size;
        this.modelPath = modelPath;
        this.modelBasicInfo = modelBasicInfo;
    }

    @Override
    public ModelBasicInfo getModelBasicInfo() {
        return modelBasicInfo;
    }

    @Override
    public String postProcessBeforeModel(Input input) throws Exception {
        Param param = getRequestParams().get(0);
        String text = input.getAsString(param.getName());
        return text;
    }

    @Override
    public Output postProcessAfterModel(Input input, HttpResponse<InputStream> httpResponse) throws Exception {
        Output output = new Output();
        output.add("text", new LlamaCppSupplier(httpResponse));
        return output;
    }

    @Override
    public List<Criteria> getCriteriaList() {
        Criteria<String, Object> llamaCpp =
                Criteria.builder()
                        .setTypes(String.class, Object.class)
                        .optModelPath(getModelPath(modelPath))
                        .optTranslatorFactory(new LlamaCppTranslatorFactory())
                        .optEngine("LlamaCpp")
                        .build();
        return Arrays.asList(llamaCpp);
    }

    @Override
    public List<EngineEnum> getEngineList() {
        return Arrays.asList(EngineEnum.LlamaCpp);
    }

    @Override
    public ModelTypeEnum getModelType() {
        return ModelTypeEnum.LLM;
    }

    @Override
    public List<Param> getRequestParams() {
        List<Param> params = new ArrayList<>();
        params.add(new Param("text", FileExtension.TEXT));
        params.addAll(inferenceParams);
        return params;
    }

    @Override
    public List<Param> getResponseParams() {
        List<Param> params = new ArrayList<>();
        params.add(new Param("text", FileExtension.STREAM));
        return params;
    }

    @Override
    public List<Param> getLoadModelParams() {
        return modelParams;
    }

    public String getModelName() {
        return modelName;
    }

    public String getSize() {
        return size;
    }
}
