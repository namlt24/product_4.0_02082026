package com.bccs.gatewaymanager.exception;

import lombok.Getter;

/** Loi nghiep vu (du lieu client gui khong hop le ve mat business, path da ton tai...). HTTP 400. */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
