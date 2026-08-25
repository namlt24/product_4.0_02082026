package com.viettel.bccs.productcatalog.utils;

import com.viettel.bccs.common.error.exception.ValidationException;

import java.util.Collection;
import java.util.regex.Pattern;


public final class RequestValidator {

    private RequestValidator() {
    }

    public static void requireNotBlank(String value, String field, String errorCode) {
        if (DataUtil.isNullOrEmpty(value)) {
            throw ValidationException.withArgs(errorCode, field);
        }
    }

    public static void requireNotNull(Object value, String field, String errorCode) {
        if (value == null) {
            throw ValidationException.withArgs(errorCode, field);
        }
    }

    public static void requireNotEmpty(Collection<?> value, String field, String errorCode) {
        if (DataUtil.isNullOrEmpty(value)) {
            throw ValidationException.withArgs(errorCode, field);
        }
    }

    public static void checkMaxLength(String value, String field, int max, String errorCode) {
        if (value != null && value.length() > max) {
            throw ValidationException.withArgs(errorCode, field, "tối đa " + max + " ký tự");
        }
    }

    public static void checkSize(Collection<?> value, String field, int max, String errorCode) {
        if (value != null && value.size() > max) {
            throw ValidationException.withArgs(errorCode, field, "tối đa " + max + " phần tử");
        }
    }

    public static void checkRange(Long value, String field, long min, long max, String errorCode) {
        if (value != null && (value < min || value > max)) {
            throw ValidationException.withArgs(errorCode, field, min + " - " + max);
        }
    }

    public static void checkRange(Integer value, String field, int min, int max, String errorCode) {
        if (value != null && (value < min || value > max)) {
            throw ValidationException.withArgs(errorCode, field, min + " - " + max);
        }
    }

    public static void checkPattern(String value, String field, Pattern pattern, String errorCode) {
        if (value != null && !value.isEmpty() && !pattern.matcher(value).matches()) {
            throw ValidationException.withArgs(errorCode, field);
        }
    }
}
