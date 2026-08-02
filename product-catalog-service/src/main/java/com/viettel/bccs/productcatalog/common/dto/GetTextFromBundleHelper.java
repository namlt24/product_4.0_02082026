package com.viettel.bccs.productcatalog.common.dto;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class GetTextFromBundleHelper {

    private static MessageSource messageSource;

    public GetTextFromBundleHelper(MessageSource messageSource) {
        GetTextFromBundleHelper.messageSource = messageSource;
    }

    public static String getText(String key) {
        if (messageSource == null) {
            return "";
        }
        try {
            return messageSource.getMessage(key, null, "", LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return "";
        }
    }

    public static String getTextParam(String key, String... params) {
        if (messageSource == null) {
            return "";
        }
        try {
            return messageSource.getMessage(key, params, "", LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return "";
        }
    }
}