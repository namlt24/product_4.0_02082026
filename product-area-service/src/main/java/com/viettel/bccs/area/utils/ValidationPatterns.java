package com.viettel.bccs.area.utils;

import java.util.regex.Pattern;

/**
 * Regex dùng chung cho {@link RequestValidator#checkPattern}. Độ dài đã được validate riêng bằng
 * {@link RequestValidator#checkMaxLength} nên các pattern ở đây chỉ cần kiểm tra charset, không cần
 * lặp lại giới hạn {0,N} như annotation @Pattern gốc trên DTO/param.
 */
public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    /** Chỉ gồm chữ và số (areaCode/parentCode/province...). */
    public static final Pattern ALPHANUMERIC = Pattern.compile("^[A-Za-z0-9]*$");
}
