package com.viettel.bccs.organization.channeltype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.channeltype.dto.ChannelTypeDTO;
import com.viettel.bccs.organization.channeltype.service.ChannelTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organization-resource-service/v1/channel-type")
@RequiredArgsConstructor
public class ChannelTypeController {

    private final ChannelTypeService channelTypeService;

    @GetMapping("/getActiveById/{channelTypeId}")
    public StandardResponse<ChannelTypeDTO> getActiveById(@PathVariable Long channelTypeId) {
        return StandardResponses.success(channelTypeService.getActiveById(channelTypeId));
    }

}