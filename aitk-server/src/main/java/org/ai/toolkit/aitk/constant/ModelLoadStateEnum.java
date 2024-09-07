package org.ai.toolkit.aitk.constant;

public enum ModelLoadStateEnum {
    /**
     * 待处理状态。
     */
    PENDING(0),

    /**
     * 下载中状态。
     */
    DOWNLOAD(1),

    /**
     * 加载中状态。
     */
    LOADING(2),

    /**
     * 成功状态。
     */
    SUCCESS(10),

    /**
     * 无效状态。
     */
    NONE(11);

    /**
     * 状态对应的整数值。
     */
    private final int value;

    /**
     * 构造函数，将每个枚举实例与一个整数值关联起来。
     *
     * @param value 与枚举实例关联的整数值。
     */
    ModelLoadStateEnum(int value) {
        this.value = value;
    }

    /**
     * 获取与枚举实例关联的整数值。
     *
     * @return 与枚举实例关联的整数值。
     */
    public int getValue() {
        return value;
    }
}
