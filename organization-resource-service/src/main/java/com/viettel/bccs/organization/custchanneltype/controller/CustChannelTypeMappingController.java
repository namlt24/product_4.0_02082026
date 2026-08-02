package com.viettel.bccs.organization.custchanneltype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.custchanneltype.dto.CustChannelTypeMappingDTO;
import com.viettel.bccs.organization.custchanneltype.service.CustChannelTypeMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization-resource-service/v1/cust-channel-type-mapping")
@RequiredArgsConstructor
public class CustChannelTypeMappingController {

    private final CustChannelTypeMappingService mappingService;

    @GetMapping("/getAllActive")
    public StandardResponse<List<CustChannelTypeMappingDTO>> getAllActive() {
        return StandardResponses.success(mappingService.getAllActive());
    }

    @GetMapping("/getByChannelTypeId/{channelTypeId}")
    public StandardResponse<List<CustChannelTypeMappingDTO>> getByChannelTypeId(@PathVariable Long channelTypeId) {
        return StandardResponses.success(mappingService.getByChannelTypeId(channelTypeId));
    }

    @GetMapping("/getByCustTypeAndChannelType")
    public StandardResponse<CustChannelTypeMappingDTO> getByCustTypeAndChannelType(
            @RequestParam String custType,
            @RequestParam Long channelTypeId) {
        return StandardResponses.success(mappingService.getByCustTypeAndChannelType(custType, channelTypeId));
    }
}