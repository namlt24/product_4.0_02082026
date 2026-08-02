package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.client.core.BccsHttpClient;
import com.viettel.bccs.policy.client.dto.StaffExtResponse;
import com.viettel.bccs.policy.client.dto.StandardClientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffExtClientImpl implements StaffExtClient {

    private final BccsHttpClient bccsHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public StaffExtResponse getStaffExtByStaffIDAndKey(Long staffId, String key) {
        try {
            var response = bccsHttpClient.get(
                    "organization-resource-service",
                    "/organization-resource-service/v1/staffext/getStaffExtByStaffIDAndKey?staffId={0}&key={1}",
                    StandardClientResponse.class,
                    staffId, key);
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), StaffExtResponse.class);
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getStaffExtByStaffIDAndKey for staffId: {}, key: {}", staffId, key, e);
            return null;
        }
    }
}