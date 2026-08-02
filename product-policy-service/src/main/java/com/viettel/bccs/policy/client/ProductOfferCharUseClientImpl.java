package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.client.core.BccsHttpClient;
import com.viettel.bccs.policy.client.dto.ProductSpecCharDTO;
import com.viettel.bccs.policy.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductOfferCharUseClientImpl implements ProductOfferCharUseClient {

    private final BccsHttpClient bccsHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "productOfferCharUseCache",
               key = "'PRODUCT_SPEC_CHAR:' + T(java.lang.String).join(',', #offeringIds.stream().sorted().toList())")
    public Map<Long, List<ProductSpecCharDTO>> getProductSpecCharByOfferingIds(List<String> offeringIds) {
        try {
            var response = bccsHttpClient.post(
                    "product-catalog-service",
                    "/product-catalog-service/v1/product-offer-char-use/getProductSpecCharByOfferingIds",
                    offeringIds,
                    StandardClientResponse.class);
            if (response != null && response.getData() != null) {
                var stringKeyMap = objectMapper.convertValue(response.getData(),
                        new TypeReference<Map<String, List<ProductSpecCharDTO>>>() {});
                return stringKeyMap.entrySet().stream()
                        .collect(Collectors.toMap(e -> Long.parseLong(e.getKey()), Map.Entry::getValue));
            }
            return Collections.emptyMap();
        } catch (RuntimeException e) {
            log.error("Error calling getProductSpecCharByOfferingIds: offeringIds={}", offeringIds, e);
            return Collections.emptyMap();
        }
    }
}