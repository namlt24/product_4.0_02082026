package com.bccs.gatewaymanager.exception;

/** Loi he thong (IO, Docker socket, ghi file that bai...). HTTP 500. */
public class SystemException extends RuntimeException {

    public SystemException(String message) {
        super(message);
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
