package org.ai.toolkit.aitk.modelmanager.llm;

import java.io.Serializable;
import java.util.List;

public class LlmModelDefine implements Serializable {

    private static final long serialVersionUID = -4940076571571859916L;

    private String name;

    private String description;

    private List<ModelDetail> models;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ModelDetail> getModels() {
        return models;
    }

    public void setModels(List<ModelDetail> models) {
        this.models = models;
    }
}
