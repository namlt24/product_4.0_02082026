package com.viettel.bccs.productcatalog.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;
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

    private final ProductPolicyFeignClient productPolicyFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<String> findSaleServiceCodeByReason(Long reasonId) {
        try {
            var response = productPolicyFeignClient.findSaleServiceCodeByReason(reasonId).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<String>>() {});
            }

            return null;
        } catch (RuntimeException e) {
            log.error("Error calling findSaleServiceCodeByReason for reasonId={}", reasonId, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling product-policy-service findSaleServiceCodeByReason for reasonId=" + reasonId, e);
        }
    }

    @Override
    public List<ReasonDTO> getMappingReasonProductOfferPrice(Long productPackageId) {
        try {
            var response = productPolicyFeignClient.getMappingReasonProductOfferPrice(productPackageId).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<ReasonDTO>>() {});
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getMappingReasonProductOfferPrice for productPackageId={}", productPackageId, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling product-policy-service getMappingReasonProductOfferPrice for productPackageId=" + productPackageId, e);
        }
    }
}