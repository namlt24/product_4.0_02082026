package com.viettel.bccs.organization.client;

import com.viettel.bccs.organization.client.dto.OptionSetValueResponse;

import java.util.List;

public interface OptionSetClient {

    List<OptionSetValueResponse> findValueByOptionSetCode(String code);
}