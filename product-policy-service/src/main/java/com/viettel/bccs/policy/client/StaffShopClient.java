package com.viettel.bccs.policy.client;

import com.viettel.bccs.policy.client.dto.StaffResponse;

public interface StaffShopClient {

    StaffResponse getStaffShopFullInfo(String staffCode);
}