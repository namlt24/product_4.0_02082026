package com.viettel.bccs.organization.custtype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.custtype.dto.CustTypeDTO;
import com.viettel.bccs.organization.custtype.service.CustTypeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization-resource-service/v1/cust-type")
@RequiredArgsConstructor
public class CustTypeController {

    private final CustTypeService custTypeService;

    @GetMapping("/findActiveByCustType/{custType}")
    public StandardResponse<CustTypeDTO> findActiveByCustType(@PathVariable String custType) {
        return StandardResponses.success(custTypeService.findActiveByCustType(custType));
    }

    @GetMapping("/getAllActive")
    public StandardResponse<List<CustTypeDTO>> getAllActive() {
        return StandardResponses.success(custTypeService.getAllActive());
    }

    @GetMapping("/getMappingChannelCustType")
    @Operation(operationId = "API_PRODUCT_010",
            summary = "API lấy mapping loại kênh với nhóm kênh",
            description = "API lấy mapping loại kênh với nhóm kênh")
    public StandardResponse<List<CustTypeDTO>> getMappingChannelCustType(
            @RequestParam Long channelTypeId,
            @RequestParam String groupType) {
        return StandardResponses.success(custTypeService.getMappingChannelCustType(channelTypeId, groupType));
    }
}