package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.policy.client.dto.ProductSpecCharLookupDTO;
import com.viettel.bccs.policy.client.dto.ProductSpecCharValueLookupDTO;
import com.viettel.bccs.policy.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductSpecCharClientImpl implements ProductSpecCharClient {

    private final ProductCatalogFeignClient productCatalogFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<ProductSpecCharLookupDTO> findByIds(List<Long> ids) {
        try {
            var response = productCatalogFeignClient.findSpecCharByIds(ids).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<ProductSpecCharLookupDTO>>() {});
            }
            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error("Error calling productspecchar/findByIds: ids={}", ids, e);
            throw new IntegrationException("BCCS-SYS-PTC-0001",
                    "Error calling product-catalog-service productspecchar/findByIds for ids=" + ids, e);
        }
    }

    @Override
    public List<ProductSpecCharValueLookupDTO> findValuesByIds(List<Long> ids) {
        try {
            var response = productCatalogFeignClient.findSpecCharValueByIds(ids).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<ProductSpecCharValueLookupDTO>>() {});
            }
            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error("Error calling productspectcharvalue/findByIds: ids={}", ids, e);
            throw new IntegrationException("BCCS-SYS-PTC-0001",
                    "Error calling product-catalog-service productspectcharvalue/findByIds for ids=" + ids, e);
        }
    }
}
