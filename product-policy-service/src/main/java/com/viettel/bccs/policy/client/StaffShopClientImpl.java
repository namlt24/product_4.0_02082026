package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.client.core.BccsHttpClient;
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

    private final BccsHttpClient bccsHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable(value = "staffShopClientCache", key = "#staffCode")
    public StaffResponse getStaffShopFullInfo(String staffCode) {
        try {
            var response = bccsHttpClient.get(
                    "organization-resource-service",
                    "/organization-resource-service/v1/staff/getStaffShopFullInfo/{staffCode}",
                    StandardClientResponse.class,
                    staffCode);
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), StaffResponse.class);
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getStaffShopFullInfo for staffCode: {}", staffCode, e);
            return null;
        }
    }
}