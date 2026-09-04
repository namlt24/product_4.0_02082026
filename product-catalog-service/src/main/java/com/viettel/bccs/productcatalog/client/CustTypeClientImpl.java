package com.viettel.bccs.productcatalog.client;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.client.error.BccsClient4xxException;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.productcatalog.client.dto.CustTypeDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustTypeClientImpl implements CustTypeClient {

    private final OrganizationResourceFeignClient organizationResourceFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<CustTypeDTO> findActiveByCustType(String custType, String status) {
        try {
            var response = organizationResourceFeignClient.findActiveByCustType(custType).getBody();
            if (response != null && response.getData() != null) {
                CustTypeDTO dto = objectMapper.convertValue(response.getData(), CustTypeDTO.class);
                return Optional.of(dto);
            }
            return Optional.empty();
        } catch (BccsClient4xxException e) {
            log.info("custType không hợp lệ hoặc không tồn tại (HTTP {}): custType={}, errorCode={}",
                    e.getStatusCode(), custType, e.getErrorCode());
            return Optional.empty();
        } catch (RuntimeException e) {
            log.error("Error calling findActiveByCustType for custType={}, status={}", custType, status, e);
            throw new IntegrationException("BCCS-SYS-CTR-0001",
                    "Error calling organization-resource-service findActiveByCustType for custType=" + custType, e);
        }
    }
}