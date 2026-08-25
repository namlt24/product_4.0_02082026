package com.viettel.bccs.organization.utils;

import com.viettel.bccs.common.error.exception.ValidationException;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Validate thủ công cho field/param nhận input từ client, thay cho Jakarta Bean Validation
 * (@Size/@Min/@Max/@Pattern/@NotNull/@NotEmpty vẫn giữ trên DTO/param chỉ để sinh Swagger doc và
 * pass OpenApiComplianceTest — KHÔNG còn enforce runtime vì @Valid/@Validated đã được gỡ).
 *
 * <p>Ném {@link ValidationException} với 1 trong 4 mã dùng chung theo loại rule
 * (BCCS-PRODUCT-VALIDATE-0000/0001/0002/0003), message tham số hoá bằng {0}/{1} nên
 * client luôn nhận được code + message cụ thể ngay ở cấp cao nhất của response, không phải đọc
 * field breakdown trong {@code data[]} như khi để framework tự bắt lỗi.
 */
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
