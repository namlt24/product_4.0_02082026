package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.client.core.BccsHttpClient;
import com.viettel.bccs.policy.client.dto.StandardClientResponse;
import com.viettel.bccs.policy.client.dto.TelecomServiceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TelecomServiceClientImpl implements TelecomServiceClient {

    private final BccsHttpClient bccsHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "telecomServiceClientCache", key = "#alias")
    public Long getServiceIdByAlias(String alias) {
        try {
            var response = bccsHttpClient.get(
                    "product-catalog-service",
                    "/product-catalog-service/v1/telecom-service/getTelServiceByAlias?alias={alias}",
                    StandardClientResponse.class,
                    alias);
            if (response != null && response.getData() != null) {
                TelecomServiceDTO dto = objectMapper.convertValue(response.getData(), TelecomServiceDTO.class);
                return dto != null ? dto.getTelecomServiceId() : null;
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getTelServiceByAlias: alias={}", alias, e);
            return null;
        }
    }
}
