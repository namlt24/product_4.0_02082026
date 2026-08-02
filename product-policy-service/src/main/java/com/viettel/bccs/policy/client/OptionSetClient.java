package com.viettel.bccs.policy.client;

import com.viettel.bccs.policy.client.dto.OptionSetValueResponse;

import java.util.List;
import java.util.Map;

public interface OptionSetClient {

    List<OptionSetValueResponse> findValueByOptionSetCode(String code);

    Map<String, List<OptionSetValueResponse>> findByOptionSetCodes(List<String> codes);


    String getValueByTwoCodeOption(String optSetCode, String code);
}