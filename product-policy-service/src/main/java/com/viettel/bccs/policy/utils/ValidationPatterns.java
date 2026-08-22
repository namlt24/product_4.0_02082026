package com.viettel.bccs.policy.utils;

import java.util.regex.Pattern;

/**
 * Regex dùng chung cho {@link RequestValidator#checkPattern}. Độ dài đã được validate riêng bằng
 * {@link RequestValidator#checkMaxLength} nên các pattern ở đây chỉ cần kiểm tra charset, không cần
 * lặp lại giới hạn {0,N} như annotation @Pattern gốc trên DTO.
 */
public final class ValidationPatterns {

    private ValidationPatterns() {
    }

    /** Mã/code dạng chữ-số-gạch dưới-gạch ngang (staffCode, actionCode, productCode, province...). */
    public static final Pattern CODE = Pattern.compile("^[A-Za-z0-9_-]*$");

    /** Free-text, chỉ chặn control-char (captchaAnswer, stationCodes, note...). */
    public static final Pattern FREE_TEXT = Pattern.compile("^[^\\x00-\\x1F\\x7F]*$");

    /** Chỉ gồm chữ số (payType...). */
    public static final Pattern DIGITS = Pattern.compile("^[0-9]*$");

    /** Chỉ gồm chữ và số, không gạch dưới/gạch ngang (payType dạng GetProductCodeRequest...). */
    public static final Pattern ALPHANUMERIC = Pattern.compile("^[A-Za-z0-9]*$");

    /** Ngày dạng dd/MM/yyyy. */
    public static final Pattern DATE_DDMMYYYY = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$");

    /** Username kiểu createUser/updateUser: chữ, số, '.', '_' hoặc '-'. */
    public static final Pattern USER_CODE = Pattern.compile("^[A-Za-z0-9._-]*$");

    /** Tên property/entity kiểu FilterRequest: chữ, số, '_' hoặc '.'. */
    public static final Pattern PROPERTY_CODE = Pattern.compile("^[A-Za-z0-9_.]*$");

    /** payType chỉ nhận 1 (trả sau) hoặc 2 (trả trước). */
    public static final Pattern PAY_TYPE_12 = Pattern.compile("^[12]?$");
}
