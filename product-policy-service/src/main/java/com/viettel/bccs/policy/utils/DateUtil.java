package com.viettel.bccs.policy.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DateUtil {

    private DateUtil() {
    }

    public static boolean isValidDateFormat(String value, String pattern) {
        if (value == null || value.isBlank()) {
            return false;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        sdf.setLenient(false);
        try {
            sdf.parse(value);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
