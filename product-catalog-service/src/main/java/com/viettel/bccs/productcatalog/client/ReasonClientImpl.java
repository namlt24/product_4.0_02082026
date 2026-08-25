package com.viettel.bccs.productcatalog.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReasonClientImpl implements ReasonClient {

    private final ProductPolicyFeignClient productPolicyFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public Long getReasonIdByTypeAndCode(String reasonCode, String actionCode, Long telecomServiceId) {
        try {
            var response = productPolicyFeignClient.getReasonIdByTypeAndCode(reasonCode, actionCode, telecomServiceId).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), Long.class);
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getReasonIdByTypeAndCode for reasonCode={}, actionCode={}, telecomServiceId={}",
                    reasonCode, actionCode, telecomServiceId, e);
            throw new IntegrationException("BCCS-SYS-CTP-0001",
                    "Error calling product-policy-service getReasonIdByTypeAndCode for reasonCode=" + reasonCode, e);
        }
    }
}
