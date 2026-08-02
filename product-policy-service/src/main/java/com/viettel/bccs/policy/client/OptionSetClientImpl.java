package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.client.core.BccsHttpClient;
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

    private final BccsHttpClient bccsHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "optionSetClientCache", key = "'CODE:' + #code")
    public List<OptionSetValueResponse> findValueByOptionSetCode(String code) {
        try {
            var response = bccsHttpClient.get(
                    "product-catalog-service",
                    "/product-catalog-service/v1/optionsetvalue/findByOptionSetCode/{code}",
                    StandardClientResponse.class,
                    code);
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(
                        response.getData(),
                        new TypeReference<List<OptionSetValueResponse>>() {});
            }
            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error("Error calling option set service for code: {}", code, e);
            return Collections.emptyList();
        }
    }

    @Override
    @Cacheable(value = "optionSetClientCache", key = "'CODES:' + T(String).join(',', #codes.stream().sorted().toList())")
    public Map<String, List<OptionSetValueResponse>> findByOptionSetCodes(List<String> codes) {
        try {
            var response = bccsHttpClient.get(
                    "product-catalog-service",
                    "/product-catalog-service/v1/optionsetvalue/findByOptionSetCodes?codes={codes}",
                    StandardClientResponse.class,
                    codes);
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(
                        response.getData(),
                        new TypeReference<Map<String, List<OptionSetValueResponse>>>() {});
            }
            return Collections.emptyMap();
        } catch (RuntimeException e) {
            log.error("Error calling findByOptionSetCodes for codes: {}", codes, e);
            return Collections.emptyMap();
        }
    }

    @Override
    @Cacheable(value = "optionSetClientCache", key = "'TWO_CODE:' + #optSetCode + ':' + #code")
    public String getValueByTwoCodeOption(String optSetCode, String code) {
        try {
            var response = bccsHttpClient.get(
                    "product-catalog-service",
                    "/product-catalog-service/v1/optionsetvalue/getValueByTwoCodeOption?optSetCode={optSetCode}&name={name}",
                    StandardClientResponse.class,
                    optSetCode, code);
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<String>() {});
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getValueByTwoCodeOption: optSetCode={}, code={}", optSetCode, code, e);
            return null;
        }
    }
}