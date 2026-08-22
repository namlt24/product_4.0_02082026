package com.viettel.bccs.organization.identitytype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.identitytype.dto.IdentityTypeDTO;
import com.viettel.bccs.organization.identitytype.service.IdentityTypeService;
import com.viettel.bccs.organization.utils.RequestValidator;
import com.viettel.bccs.organization.utils.ValidationPatterns;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.viettel.bccs.organization.identitytype.openapi.IdentityTypeControllerExamples.*;

@RestController
@RequestMapping("/organization-resource-service/v1/identity-type")
@RequiredArgsConstructor
@Tag(name = "IdentityType", description = "Tra cứu loại giấy tờ (IDENTITY_TYPE)")
public class IdentityTypeController {

    private final IdentityTypeService identityTypeService;

    @GetMapping("/getListIdentityType")
    @Operation(operationId = "API_DAUNOI_TT_PRODUCT_002 ",
            summary = "API lấy danh sách loại giấy tờ",
            description = "API lấy danh sách loại giấy tờ theo loại khách hàng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = IDENTITY_TYPE_LIST_EXAMPLE)))
    })
    public StandardResponse<List<IdentityTypeDTO>> getListIdentityType(
            @Parameter(description = "Loại khách hàng", example = "01")
            @RequestParam(required = false)
            String custType) {
        RequestValidator.checkMaxLength(custType, "custType", 6, "BCCS-ORGANIZATION-VALIDATE-SIZE");
        RequestValidator.checkPattern(custType, "custType", ValidationPatterns.CODE, "BCCS-ORGANIZATION-VALIDATE-PATTERN");
        return StandardResponses.success(identityTypeService.getListIdentityType(custType));
    }

    @GetMapping("/findByIdType")
    @Operation(operationId = "findByIdType",
            summary = "API lấy thông tin loại giấy tờ theo mã",
            description = "Trả về thông tin chi tiết 1 loại giấy tờ (IDENTITY_TYPE) đang hiệu lực (status = 1) theo mã loại giấy tờ (idType). Trả lỗi nếu không tìm thấy hoặc loại giấy tờ đã inactive.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = IDENTITY_TYPE_SINGLE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy loại giấy tờ với mã tương ứng")
    })
    public StandardResponse<IdentityTypeDTO> findByIdType(
            @Parameter(description = "Mã loại giấy tờ", example = "IDC", required = true)
            @RequestParam(required = false)
            String idType) {
        RequestValidator.requireNotBlank(idType, "idType", "BCCS-ORGANIZATION-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(idType, "idType", 10, "BCCS-ORGANIZATION-VALIDATE-SIZE");
        RequestValidator.checkPattern(idType, "idType", ValidationPatterns.CODE, "BCCS-ORGANIZATION-VALIDATE-PATTERN");
        return StandardResponses.success(identityTypeService.findByIdType(idType));
    }
}
