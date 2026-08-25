package com.viettel.bccs.organization.custtype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.custtype.dto.CustTypeDTO;
import com.viettel.bccs.organization.custtype.openapi.ApiFindActiveByCustType;
import com.viettel.bccs.organization.custtype.openapi.ApiGetAllActive;
import com.viettel.bccs.organization.custtype.openapi.ApiGetMappingChannelCustType;
import com.viettel.bccs.organization.custtype.service.CustTypeService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization-resource-service/v1/cust-type")
@RequiredArgsConstructor
@Tag(name = "CustType", description = "Tra cứu loại khách hàng (CUST_TYPE)")
public class CustTypeController {

    private final CustTypeService custTypeService;

    @ApiFindActiveByCustType
    @GetMapping("/findActiveByCustType/{custType}")
    public StandardResponse<CustTypeDTO> findActiveByCustType(
            @Parameter(description = "Mã loại khách hàng (CUST_TYPE)", example = "PREPAI", required = true)
            @PathVariable
            String custType) {
        return StandardResponses.success(custTypeService.findActiveByCustType(custType));
    }

    @ApiGetAllActive
    @GetMapping("/getAllActive")
    public StandardResponse<List<CustTypeDTO>> getAllActive() {
        return StandardResponses.success(custTypeService.getAllActive());
    }

    @GetMapping("/getMappingChannelCustType")
    @ApiGetMappingChannelCustType
    public StandardResponse<List<CustTypeDTO>> getMappingChannelCustType(
            @Parameter(description = "ID loại kênh (CHANNEL_TYPE_ID)", example = "1", required = true)
            @RequestParam(required = false)
            Long channelTypeId,
            @Parameter(description = "Loại khách hàng (GROUP_TYPE)", example = "1", required = true)
            @RequestParam(required = false)
            String groupType) {
        return StandardResponses.success(custTypeService.getMappingChannelCustType(channelTypeId, groupType));
    }
}
