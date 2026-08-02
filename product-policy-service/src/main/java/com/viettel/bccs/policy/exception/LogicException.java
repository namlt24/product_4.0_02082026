package com.viettel.bccs.policy.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogicException extends Exception {

    private String errorCode;
    private String keyMsg;

    public LogicException() {
        super();
    }

    public LogicException(String errorCode, String keyMsg) {
        super();
        this.errorCode = errorCode;
        this.keyMsg = keyMsg;
    }

    public LogicException(String errorCode, String keyMsg, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
        this.keyMsg = keyMsg;
    }

    @Override
    public String getMessage() {
        if (keyMsg == null || keyMsg.isBlank()) {
            return errorCode;
        }
        return errorCode + ":" + keyMsg;
    }

    @Override
    public String toString() {
        return getMessage();
    }
}