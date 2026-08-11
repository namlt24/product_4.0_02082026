package com.viettel.bccs.productcatalog.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.client.core.BccsHttpClient;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.productcatalog.client.dto.CustTypeDTO;
import com.viettel.bccs.productcatalog.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustTypeClientImpl implements CustTypeClient {

    private final BccsHttpClient bccsHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<CustTypeDTO> findActiveByCustType(String custType, String status) {
        try {
            var response = bccsHttpClient.get(
                    "organization-resource-service",
                    "/v1/cust-type/findActiveByCustType/{custType}",
                    StandardClientResponse.class,
                    custType);
            if (response != null && response.getData() != null) {
                CustTypeDTO dto = objectMapper.convertValue(response.getData(), CustTypeDTO.class);
                return Optional.of(dto);
            }
            return Optional.empty();
        } catch (RuntimeException e) {
            log.error("Error calling findActiveByCustType for custType={}, status={}", custType, status, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling organization-resource-service findActiveByCustType for custType=" + custType, e);
        }
    }
}