package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.policy.client.dto.ProductOfferingDTO;
import com.viettel.bccs.policy.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductOfferingClientImpl implements ProductOfferingClient {

    private final ProductCatalogFeignClient productCatalogFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<ProductOfferingDTO> getListOfferAlterStatus(Long offerId, String changeChannel, boolean checkStatus) {
        try {
            var response = productCatalogFeignClient
                    .getListOfferAlterStatus(offerId, changeChannel, checkStatus)
                    .getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<ProductOfferingDTO>>() {});
            }
            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error("Error calling getListOfferAlterStatus: offerId={}, changeChannel={}, checkStatus={}", offerId, changeChannel, checkStatus, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling product-catalog-service getListOfferAlterStatus for offerId=" + offerId, e);
        }
    }

    @Override
    public List<ProductOfferingDTO> findByTelecomSubTypeOfferTypeCheckProductStatus(Long telecomServiceId, String subType, Long offerTypeId, boolean getActiveProduct) {
        try {
            var response = productCatalogFeignClient
                    .findByTelecomSubTypeOfferTypeCheckProductStatus(
                            telecomServiceId, subType, offerTypeId, getActiveProduct)
                    .getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<ProductOfferingDTO>>() {});
            }
            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error("Error calling findByTelecomSubTypeOfferTypeCheckProductStatus: telecomServiceId={}, subType={}, offerTypeId={}, getActiveProduct={}",
                    telecomServiceId, subType, offerTypeId, getActiveProduct, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling product-catalog-service findByTelecomSubTypeOfferTypeCheckProductStatus for telecomServiceId="
                            + telecomServiceId + ", offerTypeId=" + offerTypeId, e);
        }
    }

    @Override
    @Cacheable(value = "productOfferingClientCache", key = "'CODES_OFFER_TYPE:' + T(String).join(',', #codes.stream().sorted().toList()) + ':' + #productOfferTypeId")
    public List<ProductOfferingDTO> findByCodesAndProductOfferType(List<String> codes, Long productOfferTypeId) {
        try {
            var response = productCatalogFeignClient
                    .findByCodesAndProductOfferType(codes, productOfferTypeId)
                    .getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<ProductOfferingDTO>>() {});
            }
            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error("Error calling findByCodesAndProductOfferType: codes={}, productOfferTypeId={}", codes, productOfferTypeId, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling product-catalog-service findByCodesAndProductOfferType for codes=" + codes
                            + ", productOfferTypeId=" + productOfferTypeId, e);
        }
    }

    @Override
    @Cacheable(value = "productOfferingClientCache", key = "'IDS:' + T(String).join(',', #offerIds.stream().sorted().toList().![toString()])")
    public List<ProductOfferingDTO> findByIds(List<Long> offerIds) {
        try {
            var response = productCatalogFeignClient.findByIds(offerIds).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<ProductOfferingDTO>>() {});
            }
            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error("Error calling findByIds: offerIds={}", offerIds, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling product-catalog-service product/findByIds for offerIds=" + offerIds, e);
        }
    }
}