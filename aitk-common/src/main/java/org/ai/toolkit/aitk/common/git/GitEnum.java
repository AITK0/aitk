package org.ai.toolkit.aitk.common.git;

public enum GitEnum {
    GITHUB,
    GITEE,
    MODELSCOPE;

    public static GitEnum getGitEnum(String type) {
        for (GitEnum gitEnum : values()) {
            if (gitEnum.name().equalsIgnoreCase(type)) {
                return gitEnum;
            }
        }
        return GITEE;
    }
}
