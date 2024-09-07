package org.ai.toolkit.aitk.service.vo;

import java.io.Serializable;
import java.util.List;

public class ModelNodeDataVO implements Serializable {
    private static final long serialVersionUID = -1476406845798008339L;

    private String id;

    private String label;

    private String path;


    private List<ModelNodeDataVO> children;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<ModelNodeDataVO> getChildren() {
        return children;
    }

    public void setChildren(List<ModelNodeDataVO> children) {
        this.children = children;
    }
}
