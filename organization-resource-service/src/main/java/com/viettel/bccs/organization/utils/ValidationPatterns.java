package com.viettel.bccs.organization.utils;

import java.util.regex.Pattern;

/**
 * Regex dùng chung cho {@link RequestValidator#checkPattern}. Độ dài đã được validate riêng bằng
 * {@link RequestValidator#checkMaxLength} nên các pattern ở đây chỉ cần kiểm tra charset, không cần
 * lặp lại giới hạn {0,N} như annotation @Pattern gốc trên DTO/param.
 */
public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    /** Mã/code dạng chữ-số-gạch dưới-gạch ngang. */
    public static final Pattern CODE = Pattern.compile("^[A-Za-z0-9_-]*$");

    /** Free-text, chỉ chặn control-char. */
    public static final Pattern FREE_TEXT = Pattern.compile("^[^\\x00-\\x1F\\x7F]*$");

    /** Chỉ gồm chữ số. */
    public static final Pattern DIGITS = Pattern.compile("^[0-9]*$");

    /** Chỉ gồm chữ và số, không gạch dưới/gạch ngang. */
    public static final Pattern ALPHANUMERIC = Pattern.compile("^[A-Za-z0-9]*$");
}
