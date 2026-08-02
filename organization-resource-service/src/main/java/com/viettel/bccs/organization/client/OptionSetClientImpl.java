package com.viettel.bccs.organization.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.client.core.BccsHttpClient;
import com.viettel.bccs.organization.client.dto.OptionSetValueResponse;
import com.viettel.bccs.organization.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class OptionSetClientImpl implements OptionSetClient {

    private final BccsHttpClient bccsHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<OptionSetValueResponse> findValueByOptionSetCode(String code) {
        try {
            var response = bccsHttpClient.get(
                    "product-catalog-service",
                    "/v1/optionsetvalue/findByOptionSetCode/{code}",
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
}