package org.ai.toolkit.aitk.modelzoo.bean;

import java.io.Serializable;

public class ModelBasicInfo implements Serializable {

    private static final long serialVersionUID = 4304918934741560841L;

    private String displayName;

    private String modelDescriptionPath;

    public ModelBasicInfo(String displayName, String modelDescriptionPath) {
        this.displayName = displayName;
        this.modelDescriptionPath = modelDescriptionPath;
    }

    public ModelBasicInfo() {
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getModelDescriptionPath() {
        return modelDescriptionPath;
    }

    public void setModelDescriptionPath(String modelDescriptionPath) {
        this.modelDescriptionPath = modelDescriptionPath;
    }
}
