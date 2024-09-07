package org.ai.toolkit.aitk.service;

import ai.djl.modality.Input;

import java.util.List;

import ai.djl.modality.Output;
import org.ai.toolkit.aitk.modelzoo.executor.InferenceCallback;
import org.ai.toolkit.aitk.service.vo.LlmModelVO;
import org.ai.toolkit.aitk.service.vo.ModelLoadVO;
import org.ai.toolkit.aitk.service.vo.ModelParamVO;
import org.ai.toolkit.aitk.service.vo.ModelNodeDataVO;

public interface ModelService {

    void asyncPredict(String modelId, Input input, InferenceCallback callback);

    Output syncPredict(String modelId, Input input);

    ModelParamVO getModelParamVO(String modelId);

    List<ModelNodeDataVO> getModelTreeData();

    List<LlmModelVO> getLllModelVOByModelName(String modelName);

    ModelLoadVO getModelStateByModelId(String modelId);

    boolean startLoad(String modelId);

    boolean unloadModel(String modelId);

    List<String> getMenuPath(String modelId);
}
