package com.viettel.bccs.policy.client;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductPackageClientImpl implements ProductPackageClient {

    private final ProductCatalogFeignClient productCatalogFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<String> getPackageCodesByProductOfferTypeCount(String excludeProdOfferType, Integer packageNumber) {
        try {
            var response = productCatalogFeignClient
                    .getPackageCodesByProductOfferTypeCount(excludeProdOfferType, packageNumber)
                    .getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(
                        response.getData(),
                        new TypeReference<List<String>>() {});
            }
            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error("Error calling product-package service for excludeProdOfferType: {}, packageNumber: {}",
                    excludeProdOfferType, packageNumber, e);
            throw new IntegrationException("BCCS-SYS-PTC-0001",
                    "Error calling product-catalog-service getPackageCodesByProductOfferTypeCount for excludeProdOfferType="
                            + excludeProdOfferType + ", pNumber=" + packageNumber, e);
        }
    }
}