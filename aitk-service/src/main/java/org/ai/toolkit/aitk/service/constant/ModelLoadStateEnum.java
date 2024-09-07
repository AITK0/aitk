package org.ai.toolkit.aitk.service.constant;

public enum ModelLoadStateEnum {
    /**
     * 待加载
     */
    PENDING(0),

    /**
     * 下载中
     */
    DOWNLOAD(1),

    /**
     * 加载中
     */
    LOADING(2),

    /**
     * 加载完成可使用
     */
    SUCCESS(10),

    /**
     * 卸载中
     */
    UNLOADING(20),

    /**
     * 不存在
     */
    NONE(100);

    private final int value;

    ModelLoadStateEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
