package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.policy.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductPackageClientImpl implements ProductPackageClient {

    private final ProductCatalogFeignClient productCatalogFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<String> getPackageCodesByProductOfferTypeCount(String excludeProdOfferType, Integer pNumber) {
        try {
            var response = productCatalogFeignClient
                    .getPackageCodesByProductOfferTypeCount(excludeProdOfferType, pNumber)
                    .getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(
                        response.getData(),
                        new TypeReference<List<String>>() {});
            }
            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error("Error calling product-package service for excludeProdOfferType: {}, pNumber: {}",
                    excludeProdOfferType, pNumber, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling product-catalog-service getPackageCodesByProductOfferTypeCount for excludeProdOfferType="
                            + excludeProdOfferType + ", pNumber=" + pNumber, e);
        }
    }
}