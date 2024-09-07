package org.ai.toolkit.aitk.handler;

import org.ai.toolkit.aitk.common.errorcode.AitkErrorCode;
import org.ai.toolkit.aitk.common.exception.AitkException;
import org.ai.toolkit.aitk.vo.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class ControllerExceptionHandler {

    private static Logger LOGGER = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResultVO exceptionHandler(Throwable e) {
        LOGGER.error("ControllerExceptionHandler#exceptionHandler", e);
        if (e instanceof AitkException) {
            AitkException aitkException = (AitkException) e;
            return new ResultVO(aitkException.getAitkErrorCode().getCode(), e.getMessage());
        }
        return new ResultVO(AitkErrorCode.UNKNOWN_ERROR.getCode(), AitkErrorCode.UNKNOWN_ERROR.getMsg());
    }
}
