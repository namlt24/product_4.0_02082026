package com.viettel.bccs.policy.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Slf4j
@RequiredArgsConstructor
public class MessageUtil {

    private final MessageSource messageSource;

    public String getTextParam(String key, Object... args) {
        try {
            return messageSource.getMessage(key, args, Locale.getDefault());
        } catch (Exception e) {
            log.warn("Message not found for key: {}", key);
            return key;
        }
    }

    public String getText(String key) {
        return getTextParam(key);
    }
}