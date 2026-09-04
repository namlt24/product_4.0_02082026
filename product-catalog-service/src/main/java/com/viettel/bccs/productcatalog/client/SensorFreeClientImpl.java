package com.viettel.bccs.productcatalog.client;

import java.util.List;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.productcatalog.client.dto.SensorFeeRuleDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorFreeClientImpl implements SensorFreeClient {

    private final OrganizationResourceFeignClient organizationResourceFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<SensorFeeRuleDTO> checkReasonSensorFee(Long productPackageId) {
        try {
            var response = organizationResourceFeignClient.checkReasonSensorFee(productPackageId).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<SensorFeeRuleDTO>>() {});
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling checkReasonSensorFee for productPackageId={}", productPackageId, e);
            throw new IntegrationException("BCCS-SYS-CTR-0001",
                    "Error calling organization-resource-service checkReasonSensorFee for productPackageId="
                            + productPackageId, e);
        }
    }
}