package com.viettel.bccs.organization.custchanneltype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.custchanneltype.dto.CustChannelTypeMappingDTO;
import com.viettel.bccs.organization.custchanneltype.openapi.ApiGetAllActive;
import com.viettel.bccs.organization.custchanneltype.openapi.ApiGetByChannelTypeId;
import com.viettel.bccs.organization.custchanneltype.openapi.ApiGetByCustTypeAndChannelType;
import com.viettel.bccs.organization.custchanneltype.service.CustChannelTypeMappingService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization-resource-service/v1/cust-channel-type-mapping")
@RequiredArgsConstructor
@Tag(name = "CustChannelTypeMapping", description = "Tra cứu mapping loại khách hàng - loại kênh (CUST_CHANNEL_TYPE_MAPPING)")
public class CustChannelTypeMappingController {

    private final CustChannelTypeMappingService mappingService;

    @ApiGetAllActive
    @GetMapping("/getAllActive")
    public StandardResponse<List<CustChannelTypeMappingDTO>> getAllActive() {
        return StandardResponses.success(mappingService.getAllActive());
    }

    @ApiGetByChannelTypeId
    @GetMapping("/getByChannelTypeId/{channelTypeId}")
    public StandardResponse<List<CustChannelTypeMappingDTO>> getByChannelTypeId(
            @Parameter(description = "ID loại kênh (CHANNEL_TYPE_ID)", example = "1", required = true)
            @PathVariable
            Long channelTypeId) {
        return StandardResponses.success(mappingService.getByChannelTypeId(channelTypeId));
    }

    @ApiGetByCustTypeAndChannelType
    @GetMapping("/getByCustTypeAndChannelType")
    public StandardResponse<CustChannelTypeMappingDTO> getByCustTypeAndChannelType(
            @Parameter(description = "Mã loại khách hàng (CUST_TYPE)", example = "PREPAID", required = true)
            @RequestParam(required = false)
            String custType,
            @Parameter(description = "ID loại kênh (CHANNEL_TYPE_ID)", example = "1", required = true)
            @RequestParam(required = false)
            Long channelTypeId) {
        return StandardResponses.success(mappingService.getByCustTypeAndChannelType(custType, channelTypeId));
    }
}
