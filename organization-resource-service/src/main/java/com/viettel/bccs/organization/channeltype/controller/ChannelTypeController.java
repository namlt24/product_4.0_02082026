package com.viettel.bccs.organization.channeltype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.channeltype.dto.ChannelTypeDTO;
import com.viettel.bccs.organization.channeltype.openapi.ApiGetActiveById;
import com.viettel.bccs.organization.channeltype.service.ChannelTypeService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organization-resource-service/v1/channel-type")
@RequiredArgsConstructor
@Tag(name = "ChannelType", description = "Tra cứu loại kênh (CHANNEL_TYPE)")
public class ChannelTypeController {

    private final ChannelTypeService channelTypeService;

    @ApiGetActiveById
    @GetMapping("/getActiveById/{channelTypeId}")
    public StandardResponse<ChannelTypeDTO> getActiveById(
            @Parameter(description = "ID loại kênh (CHANNEL_TYPE_ID)", example = "1", required = true)
            @PathVariable
            Long channelTypeId) {
        return StandardResponses.success(channelTypeService.getActiveById(channelTypeId));
    }

}
