package com.viettel.bccs.organization.custtype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.custtype.dto.CustTypeDTO;
import com.viettel.bccs.organization.custtype.service.CustTypeService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.viettel.bccs.organization.custtype.openapi.CustTypeControllerExamples.*;

@RestController
@RequestMapping("/organization-resource-service/v1/cust-type")
@RequiredArgsConstructor
@Tag(name = "CustType", description = "Tra cứu loại khách hàng (CUST_TYPE)")
public class CustTypeController {

    private final CustTypeService custTypeService;

    @Operation(operationId = "findActiveByCustType", summary = "Lấy loại khách hàng đang hiệu lực theo mã",
            description = "Tra cứu 1 bản ghi CUST_TYPE đang hiệu lực (status = 1) theo CUST_TYPE (khoá chính).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = CUST_TYPE_SINGLE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy loại khách hàng với mã tương ứng")
    })
    @GetMapping("/findActiveByCustType/{custType}")
    public StandardResponse<CustTypeDTO> findActiveByCustType(
            @Parameter(description = "Mã loại khách hàng (CUST_TYPE)", example = "PREPAI", required = true)
            @PathVariable
            String custType) {
        RequestValidator.requireNotBlank(custType, "custType", "BCCS-ORGANIZATION-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(custType, "custType", 6, "BCCS-ORGANIZATION-VALIDATE-SIZE");
        RequestValidator.checkPattern(custType, "custType", ValidationPatterns.CODE, "BCCS-ORGANIZATION-VALIDATE-PATTERN");
        return StandardResponses.success(custTypeService.findActiveByCustType(custType));
    }

    @Operation(operationId = "getAllActiveCustType", summary = "Lấy toàn bộ loại khách hàng đang hiệu lực",
            description = "Trả về danh sách toàn bộ bản ghi CUST_TYPE đang hiệu lực (status = 1), không phân trang.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = CUST_TYPE_LIST_EXAMPLE)))
    })
    @GetMapping("/getAllActive")
    public StandardResponse<List<CustTypeDTO>> getAllActive() {
        return StandardResponses.success(custTypeService.getAllActive());
    }

    @GetMapping("/getMappingChannelCustType")
    @Operation(operationId = "API_PRODUCT_010",
            summary = "API lấy mapping loại kênh với nhóm kênh",
            description = "API lấy mapping loại kênh với nhóm kênh")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = CUST_TYPE_LIST_EXAMPLE)))
    })
    public StandardResponse<List<CustTypeDTO>> getMappingChannelCustType(
            @Parameter(description = "ID loại kênh (CHANNEL_TYPE_ID)", example = "1", required = true)
            @RequestParam(required = false)
            Long channelTypeId,
            @Parameter(description = "Loại khách hàng (GROUP_TYPE)", example = "1", required = true)
            @RequestParam(required = false)
            String groupType) {
        RequestValidator.checkRange(channelTypeId, "channelTypeId", 0L, 9999999999L, "BCCS-ORGANIZATION-VALIDATE-RANGE");
        RequestValidator.requireNotBlank(groupType, "groupType", "BCCS-ORGANIZATION-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(groupType, "groupType", 1, "BCCS-ORGANIZATION-VALIDATE-SIZE");
        RequestValidator.checkPattern(groupType, "groupType", ValidationPatterns.CODE, "BCCS-ORGANIZATION-VALIDATE-PATTERN");
        return StandardResponses.success(custTypeService.getMappingChannelCustType(channelTypeId, groupType));
    }
}
