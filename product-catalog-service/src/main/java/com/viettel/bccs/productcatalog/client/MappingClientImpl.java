package com.viettel.bccs.productcatalog.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.client.core.BccsHttpClient;
import com.viettel.bccs.productcatalog.client.dto.ReasonDTO;
import com.viettel.bccs.productcatalog.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MappingClientImpl implements MappingClient {

    private final BccsHttpClient bccsHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<String> findSaleServiceCodeByReason(Long reasonId) {
        try {
            var response = bccsHttpClient.get(
                    "product-policy-service",
                    "/v1/mapping/findSaleServiceCodeByReason/{reasonId}",
                    StandardClientResponse.class,
                    reasonId);
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<String>>() {});
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling findSaleServiceCodeByReason for reasonId={}", reasonId, e);
            return null;
        }
    }

    @Override
    public List<ReasonDTO> getMappingReasonProductOfferPrice(Long productPackageId) {
        try {
            var response = bccsHttpClient.get(
                    "product-policy-service",
                    "/v1/mapping/getMappingReasonProductOfferPrice/{productPackageId}",
                    StandardClientResponse.class,
                    productPackageId);
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<ReasonDTO>>() {});
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getMappingReasonProductOfferPrice for productPackageId={}", productPackageId, e);
            return null;
        }
    }
}