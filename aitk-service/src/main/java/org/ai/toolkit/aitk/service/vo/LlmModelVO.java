package org.ai.toolkit.aitk.service.vo;

import java.io.Serializable;

public class LlmModelVO implements Serializable {
    private static final long serialVersionUID = 3912365813142770522L;

    private String id;

    private String name;

    public LlmModelVO(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
