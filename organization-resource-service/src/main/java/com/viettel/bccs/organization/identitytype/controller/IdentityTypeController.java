package com.viettel.bccs.organization.identitytype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.identitytype.dto.IdentityTypeDTO;
import com.viettel.bccs.organization.identitytype.openapi.ApiFindByIdType;
import com.viettel.bccs.organization.identitytype.openapi.ApiGetListIdentityType;
import com.viettel.bccs.organization.identitytype.service.IdentityTypeService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/organization-resource-service/v1/identity-type")
@RequiredArgsConstructor
@Tag(name = "IdentityType", description = "Tra cứu loại giấy tờ (IDENTITY_TYPE)")
public class IdentityTypeController {

    private final IdentityTypeService identityTypeService;

    @GetMapping("/getListIdentityType")
    @ApiGetListIdentityType
    public StandardResponse<List<IdentityTypeDTO>> getListIdentityType(
            @Parameter(description = "Loại khách hàng", example = "01")
            @RequestParam(required = false)
            String custType) {
        return StandardResponses.success(identityTypeService.getListIdentityType(custType));
    }

    @GetMapping("/findByIdType")
    @ApiFindByIdType
    public StandardResponse<IdentityTypeDTO> findByIdType(
            @Parameter(description = "Mã loại giấy tờ", example = "IDC", required = true)
            @RequestParam(required = false)
            String idType) {
        return StandardResponses.success(identityTypeService.findByIdType(idType));
    }
}
