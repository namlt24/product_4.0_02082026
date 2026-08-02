package com.viettel.bccs.policy.client;

import com.viettel.bccs.policy.client.dto.StaffExtResponse;

public interface StaffExtClient {

    StaffExtResponse getStaffExtByStaffIDAndKey(Long staffId, String key);
}