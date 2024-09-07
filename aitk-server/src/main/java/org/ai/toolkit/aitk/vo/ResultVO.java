package org.ai.toolkit.aitk.vo;

import java.io.Serializable;

public class ResultVO<T> implements Serializable {

    private static final Integer SUCCESS_CODE = 200;

    private static final String SUCCESS_MSG = "SUCCESS";

    private static final long serialVersionUID = 8159994568030073544L;

    private Integer code;

    private String msg;

    private T data;

    public ResultVO() {
    }

    public ResultVO(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultVO(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ResultVO<T> createSuccessResultVO(T data) {
        return new ResultVO(SUCCESS_CODE, SUCCESS_MSG, data);
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
