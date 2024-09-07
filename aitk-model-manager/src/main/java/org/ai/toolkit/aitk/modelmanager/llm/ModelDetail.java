package org.ai.toolkit.aitk.modelmanager.llm;

import java.io.Serializable;

public class ModelDetail implements Serializable {
    private static final long serialVersionUID = 7959593306882418360L;

    private String name;

    private String path;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
