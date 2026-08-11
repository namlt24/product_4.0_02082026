package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.policy.client.dto.StaffExtResponse;
import com.viettel.bccs.policy.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffExtClientImpl implements StaffExtClient {

    private final OrganizationResourceFeignClient organizationResourceFeignClient;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "staffExtClientCache", key = "#staffId + ':' + #key")
    public StaffExtResponse getStaffExtByStaffIDAndKey(Long staffId, String key) {
        try {
            var response = organizationResourceFeignClient.getStaffExtByStaffIDAndKey(staffId, key).getBody();
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), StaffExtResponse.class);
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getStaffExtByStaffIDAndKey for staffId: {}, key: {}", staffId, key, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling organization-resource-service getStaffExtByStaffIDAndKey for staffId=" + staffId + ", key=" + key, e);
        }
    }
}