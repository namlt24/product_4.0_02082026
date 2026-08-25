package com.viettel.bccs.productcatalog.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.productcatalog.client.dto.FreeCamEquipmentDTO;
import com.viettel.bccs.productcatalog.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FreeCamEquipmentClientImpl implements FreeCamEquipmentClient {

    private final ProductPolicyFeignClient productPolicyFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<FreeCamEquipmentDTO> checkReasonFreeCam(Long productPackageId) {
        try {
            var response = productPolicyFeignClient.checkReasonFreeCam(productPackageId).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<FreeCamEquipmentDTO>>() {});
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling checkReasonFreeCam for productPackageId={}", productPackageId, e);
            throw new IntegrationException("BCCS-SYS-CTP-0001",
                    "Error calling product-policy-service checkReasonFreeCam for productPackageId=" + productPackageId, e);
        }
    }
}