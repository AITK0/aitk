package org.ai.toolkit.aitk.modelzoo.constant;

public enum ModelParentTypeEnum {
    CV("计算机视觉"),
    NLP("自然语言处理"),
    VOICE("语音"),
    LLM("大语言模型"),
    MULTIMODAL("多模态模型");

    private String name;

    ModelParentTypeEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
