package com.viettel.bccs.policy.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.policy.client.dto.ProductSpecCharDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductOfferCharUseClientImpl implements ProductOfferCharUseClient {

    private final ProductCatalogFeignClient productCatalogFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "productOfferCharUseCache",
               key = "'ProductSpecChar:' + T(java.lang.String).join(',', #offeringIds.stream().sorted().toList())")
    public Map<Long, List<ProductSpecCharDTO>> getProductSpecCharByOfferingIds(List<String> offeringIds) {
        try {
            var response = productCatalogFeignClient.getProductSpecCharByOfferingIds(offeringIds).getBody();
            if (response != null && response.getData() != null) {
                var stringKeyMap = objectMapper.convertValue(response.getData(),
                        new TypeReference<Map<String, List<ProductSpecCharDTO>>>() {});
                return stringKeyMap.entrySet().stream()
                        .collect(Collectors.toMap(e -> Long.parseLong(e.getKey()), Map.Entry::getValue));
            }
            return Collections.emptyMap();
        } catch (RuntimeException e) {
            log.error("Error calling getProductSpecCharByOfferingIds: offeringIds={}", offeringIds, e);
            throw new IntegrationException("BCCS-SYS-PTC-0001",
                    "Error calling product-catalog-service getProductSpecCharByOfferingIds for offeringIds=" + offeringIds, e);
        }
    }
}