package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.client.core.BccsHttpClient;
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

    private final BccsHttpClient bccsHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<String> getPackageCodesByProductOfferTypeCount(String excludeProdOfferType, Integer pNumber) {
        try {
            var response = bccsHttpClient.get(
                    "product-catalog-service",
                    "/product-catalog-service/v1/product-package/getPackageCodesByProductOfferTypeCount?excludeProdOfferType={0}&pNumber={1}",
                    StandardClientResponse.class,
                    excludeProdOfferType, pNumber);
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(
                        response.getData(),
                        new TypeReference<List<String>>() {});
            }
            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error("Error calling product-package service for excludeProdOfferType: {}, pNumber: {}",
                    excludeProdOfferType, pNumber, e);
            return Collections.emptyList();
        }
    }
}