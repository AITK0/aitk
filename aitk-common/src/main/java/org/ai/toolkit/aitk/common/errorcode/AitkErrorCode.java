package org.ai.toolkit.aitk.common.errorcode;

public enum AitkErrorCode {

    UNKNOWN_ERROR(-1, "unknown exception"),

    KNOWN_ERROR(1, ""),

    MODEL_LOAD_ERROR(10000, "model loading error"),

    PARAMETER_VALIDATION_ERROR(10001, "parameter validation error"),

    MODEL_NOT_LOADED_ERROR(10002, "Please load the model first"),

    DOWNLOAD_LLM_JSON_ERROR(20000,"Error downloading LLM model file")
    ;

    AitkErrorCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    private Integer code;

    private String msg;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    }
