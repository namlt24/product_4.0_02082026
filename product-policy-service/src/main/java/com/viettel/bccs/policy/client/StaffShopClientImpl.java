package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.policy.client.dto.StaffResponse;
import com.viettel.bccs.policy.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffShopClientImpl implements StaffShopClient {

    private final OrganizationResourceFeignClient organizationResourceFeignClient;
    private final ObjectMapper objectMapper;

    @Override
//    @Cacheable(value = "staffShopClientCache", key = "#staffCode")
    public StaffResponse getStaffShopFullInfo(String staffCode) {
        try {
            var response = organizationResourceFeignClient.getStaffShopFullInfo(staffCode).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), StaffResponse.class);
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getStaffShopFullInfo for staffCode: {}", staffCode, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling organization-resource-service getStaffShopFullInfo for staffCode=" + staffCode, e);
        }
    }

    @Override
    @Cacheable(value = "staffShopClientCache", key = "'BY_ID:' + #staffId")
    public StaffResponse getStaffShopFullInfoByStaffId(Long staffId) {
        try {
            var response = organizationResourceFeignClient.getStaffShopFullInfoByStaffId(staffId).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), StaffResponse.class);
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getStaffShopFullInfoByStaffId for staffId: {}", staffId, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling organization-resource-service getStaffShopFullInfoByStaffId for staffId=" + staffId, e);
        }
    }
}