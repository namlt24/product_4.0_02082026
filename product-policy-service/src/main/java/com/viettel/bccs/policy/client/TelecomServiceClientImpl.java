package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;
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

    private final ProductCatalogFeignClient productCatalogFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "telecomServiceClientCache", key = "#alias")
    public Long getServiceIdByAlias(String alias) {
        try {
            var response = productCatalogFeignClient.getTelServiceByAlias(alias).getBody();
            if (response != null && response.getData() != null) {
                TelecomServiceDTO dto = objectMapper.convertValue(response.getData(), TelecomServiceDTO.class);
                return dto != null ? dto.getTelecomServiceId() : null;
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getTelServiceByAlias: alias={}", alias, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling product-catalog-service getTelServiceByAlias for alias=" + alias, e);
        }
    }
}
