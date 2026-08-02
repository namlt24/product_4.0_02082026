package com.viettel.bccs.productcatalog.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.client.core.BccsHttpClient;
import com.viettel.bccs.productcatalog.client.dto.SensorFeeRuleDTO;
import com.viettel.bccs.productcatalog.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorFreeClientImpl implements SensorFreeClient {

    private final BccsHttpClient bccsHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<SensorFeeRuleDTO> checkReasonSensorFee(Long productPackageId) {
        try {
            var response = bccsHttpClient.get(
                    "organization-resource-service",
                    "/v1/sensor-fee/checkReasonSensorFee/{productPackageId}",
                    StandardClientResponse.class,
                    productPackageId);
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<SensorFeeRuleDTO>>() {});
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling checkReasonSensorFee for productPackageId={}", productPackageId, e);
            return null;
        }
    }
}