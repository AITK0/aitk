package org.ai.toolkit.aitk.modelzoo.constant;

import java.util.*;

public enum ModelTypeEnum {
    VISION_DETECTION_TRACKING("视觉检测跟踪", ModelParentTypeEnum.CV),
    OCR("光学字符识别", ModelParentTypeEnum.CV),
    HUMAN_FACE_BODY("人脸人体", ModelParentTypeEnum.CV),
    VISION_CLASSIFICATION("视觉分类", ModelParentTypeEnum.CV),
    VISION_EDITING("视觉编辑", ModelParentTypeEnum.CV),
    VISION_SEGMENTATION("视觉分割", ModelParentTypeEnum.CV),

    text_classification("文本分类", ModelParentTypeEnum.NLP),
    WORD_SEGMENTATION("分词", ModelParentTypeEnum.NLP),
    NAMED_ENTITY_RECOGNITION("命名实体识别", ModelParentTypeEnum.NLP),

    AUTO_SPEECH_RECOGNITION("语音识别", ModelParentTypeEnum.VOICE),
    TEXT_TO_SPEECH("语音合成", ModelParentTypeEnum.VOICE),
    ACOUSTIC_NOISE_SUPPRESSION("语音降噪", ModelParentTypeEnum.VOICE),

    LLM("大语言模型", ModelParentTypeEnum.LLM),

    MULTIMODAL("多模态模型", ModelParentTypeEnum.MULTIMODAL);

    private String name;

    private ModelParentTypeEnum modelParentTypeEnum;

    public static Map<ModelParentTypeEnum, List<ModelTypeEnum>> getParentTypeMap() {
        Map<ModelParentTypeEnum, List<ModelTypeEnum>> result = new HashMap<>();
        for (ModelTypeEnum modelTypeEnum : ModelTypeEnum.values()) {
            if (result.containsKey(modelTypeEnum.getModelParentTypeEnum())) {
                result.get(modelTypeEnum.getModelParentTypeEnum()).add(modelTypeEnum);
            } else {
                result.put(modelTypeEnum.getModelParentTypeEnum(), new ArrayList<>(Arrays.asList(modelTypeEnum)));
            }
        }
        return result;
    }

    ModelTypeEnum(String name, ModelParentTypeEnum modelParentTypeEnum) {
        this.name = name;
        this.modelParentTypeEnum = modelParentTypeEnum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ModelParentTypeEnum getModelParentTypeEnum() {
        return modelParentTypeEnum;
    }

    public void setModelParentTypeEnum(ModelParentTypeEnum modelParentTypeEnum) {
        this.modelParentTypeEnum = modelParentTypeEnum;
    }
}
