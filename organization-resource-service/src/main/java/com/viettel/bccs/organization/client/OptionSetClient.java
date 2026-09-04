package com.viettel.bccs.organization.client;

import java.util.List;

import com.viettel.bccs.organization.client.dto.OptionSetValueResponse;

public interface OptionSetClient {

    List<OptionSetValueResponse> findValueByOptionSetCode(String code);
}
