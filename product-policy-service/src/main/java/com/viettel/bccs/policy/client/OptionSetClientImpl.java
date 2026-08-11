package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.policy.client.dto.OptionSetValueResponse;
import com.viettel.bccs.policy.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class OptionSetClientImpl implements OptionSetClient {

    private final ProductCatalogFeignClient productCatalogFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "optionSetClientCache", key = "'CODE:' + #code")
    public List<OptionSetValueResponse> findValueByOptionSetCode(String code) {
        try {
            var response = productCatalogFeignClient.findValueByOptionSetCode(code).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(
                        response.getData(),
                        new TypeReference<List<OptionSetValueResponse>>() {});
            }
            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error("Error calling option set service for code: {}", code, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling product-catalog-service findValueByOptionSetCode for code=" + code, e);
        }
    }

    @Override
    @Cacheable(value = "optionSetClientCache", key = "'CODES:' + T(String).join(',', #codes.stream().sorted().toList())")
    public Map<String, List<OptionSetValueResponse>> findByOptionSetCodes(List<String> codes) {
        try {
            var response = productCatalogFeignClient.findByOptionSetCodes(codes).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(
                        response.getData(),
                        new TypeReference<Map<String, List<OptionSetValueResponse>>>() {});
            }
            return Collections.emptyMap();
        } catch (RuntimeException e) {
            log.error("Error calling findByOptionSetCodes for codes: {}", codes, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling product-catalog-service findByOptionSetCodes for codes=" + codes, e);
        }
    }

    @Override
    @Cacheable(value = "optionSetClientCache", key = "'TWO_CODE:' + #optSetCode + ':' + #code")
    public String getValueByTwoCodeOption(String optSetCode, String code) {
        try {
            var response = productCatalogFeignClient.getValueByTwoCodeOption(optSetCode, code).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<String>() {});
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getValueByTwoCodeOption: optSetCode={}, code={}", optSetCode, code, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling product-catalog-service getValueByTwoCodeOption for optSetCode=" + optSetCode + ", code=" + code, e);
        }
    }
}