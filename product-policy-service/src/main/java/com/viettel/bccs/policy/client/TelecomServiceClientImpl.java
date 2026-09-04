package com.viettel.bccs.policy.client;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelecomServiceClientImpl implements TelecomServiceClient {


    private final TelecomServiceIdLookupCacheService cacheService;

    @Override
    public Long getServiceIdByAlias(String alias) {
        String idStr = cacheService.getServiceIdByAliasCached(alias);
        return idStr != null ? Long.valueOf(idStr) : null;
    }
}
