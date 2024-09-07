package org.ai.toolkit.aitk.service.vo;

import java.io.Serializable;
import java.util.List;
import org.ai.toolkit.aitk.modelzoo.bean.ModelBasicInfo;
import org.ai.toolkit.aitk.modelzoo.bean.Param;

public class ModelParamVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Param> loadModelParams;

    private List<Param> requestParams;

    private List<Param> responseParams;

    private ModelBasicInfo modelBasicInfo;

    public ModelParamVO() {
    }


    public ModelParamVO(List<Param> loadModelParams, List<Param> requestParams,
        List<Param> responseParams, ModelBasicInfo modelBasicInfo) {
        this.loadModelParams = loadModelParams;
        this.requestParams = requestParams;
        this.responseParams = responseParams;
        this.modelBasicInfo = modelBasicInfo;
    }

    public List<Param> getLoadModelParams() {
        return loadModelParams;
    }

    public void setLoadModelParams(List<Param> loadModelParams) {
        this.loadModelParams = loadModelParams;
    }

    public List<Param> getRequestParams() {
        return requestParams;
    }

    public void setRequestParams(List<Param> requestParams) {
        this.requestParams = requestParams;
    }

    public List<Param> getResponseParams() {
        return responseParams;
    }

    public void setResponseParams(List<Param> responseParams) {
        this.responseParams = responseParams;
    }

    public ModelBasicInfo getModelBasicInfo() {
        return modelBasicInfo;
    }

    public void setModelBasicInfo(ModelBasicInfo modelBasicInfo) {
        this.modelBasicInfo = modelBasicInfo;
    }
}
