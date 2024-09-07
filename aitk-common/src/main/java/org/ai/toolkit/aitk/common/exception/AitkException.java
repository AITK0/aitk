package org.ai.toolkit.aitk.common.exception;

import org.ai.toolkit.aitk.common.errorcode.AitkErrorCode;

public class AitkException extends RuntimeException {

    private AitkErrorCode aitkErrorCode;

    public AitkException(AitkErrorCode aitkErrorCode, Throwable cause) {
        super(aitkErrorCode.getMsg(), cause);
        this.aitkErrorCode = aitkErrorCode;
    }

    public AitkException(AitkErrorCode aitkErrorCode, String msg, Throwable cause) {
        super(msg, cause);
        this.aitkErrorCode = aitkErrorCode;
    }

    public AitkException(AitkErrorCode aitkErrorCode, String msg) {
        super(msg);
        this.aitkErrorCode = aitkErrorCode;
    }

    public AitkException(AitkErrorCode aitkErrorCode) {
        super(aitkErrorCode.getMsg());
        this.aitkErrorCode = aitkErrorCode;
    }

    public AitkErrorCode getAitkErrorCode() {
        return aitkErrorCode;
    }

    public void setAitkErrorCode(AitkErrorCode aitkErrorCode) {
        this.aitkErrorCode = aitkErrorCode;
    }
}
