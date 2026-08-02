package com.viettel.bccs.organization.identitytype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.identitytype.dto.IdentityTypeDTO;
import com.viettel.bccs.organization.identitytype.service.IdentityTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/organization-resource-service/v1/identity-type")
@RequiredArgsConstructor
public class IdentityTypeController {

    private final IdentityTypeService identityTypeService;

    @GetMapping("/getListIdentityType")
    @Operation(operationId = "API_DAUNOI_TT_PRODUCT_002 ",
            summary = "API lấy danh sách loại giấy tờ",
            description = "API lấy danh sách loại giấy tờ theo loại khách hàng")
    public StandardResponse<List<IdentityTypeDTO>> getListIdentityType(
            @Parameter(description = "Loại khách hàng", example = "01")
            @RequestParam(required = false) String custType) {
        return StandardResponses.success(identityTypeService.getListIdentityType(custType));
    }
}