package com.viettel.bccs.organization.custchanneltype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.custchanneltype.dto.CustChannelTypeMappingDTO;
import com.viettel.bccs.organization.custchanneltype.service.CustChannelTypeMappingService;
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

import static com.viettel.bccs.organization.custchanneltype.openapi.CustChannelTypeMappingControllerExamples.*;

@RestController
@RequestMapping("/organization-resource-service/v1/cust-channel-type-mapping")
@RequiredArgsConstructor
@Tag(name = "CustChannelTypeMapping", description = "Tra cứu mapping loại khách hàng - loại kênh (CUST_CHANNEL_TYPE_MAPPING)")
public class CustChannelTypeMappingController {

    private final CustChannelTypeMappingService mappingService;

    @Operation(operationId = "getAllActiveCustChannelTypeMapping", summary = "Lấy toàn bộ mapping đang hiệu lực",
            description = "Trả về danh sách toàn bộ bản ghi CUST_CHANNEL_TYPE_MAPPING đang hiệu lực (status = 1).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = MAPPING_LIST_EXAMPLE)))
    })
    @GetMapping("/getAllActive")
    public StandardResponse<List<CustChannelTypeMappingDTO>> getAllActive() {
        return StandardResponses.success(mappingService.getAllActive());
    }

    @Operation(operationId = "getCustChannelTypeMappingByChannelTypeId", summary = "Lấy danh sách mapping theo ID loại kênh",
            description = "Tra cứu các bản ghi CUST_CHANNEL_TYPE_MAPPING theo CHANNEL_TYPE_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = MAPPING_LIST_EXAMPLE)))
    })
    @GetMapping("/getByChannelTypeId/{channelTypeId}")
    public StandardResponse<List<CustChannelTypeMappingDTO>> getByChannelTypeId(
            @Parameter(description = "ID loại kênh (CHANNEL_TYPE_ID)", example = "1", required = true)
            @PathVariable
            Long channelTypeId) {
        RequestValidator.checkRange(channelTypeId, "channelTypeId", 0L, 9999999999L, "BCCS-ORGANIZATION-VALIDATE-RANGE");
        return StandardResponses.success(mappingService.getByChannelTypeId(channelTypeId));
    }

    @Operation(operationId = "getByCustTypeAndChannelType", summary = "Lấy mapping theo loại khách hàng và loại kênh",
            description = "Tra cứu 1 bản ghi CUST_CHANNEL_TYPE_MAPPING theo cặp CUST_TYPE và CHANNEL_TYPE_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = MAPPING_SINGLE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy mapping tương ứng")
    })
    @GetMapping("/getByCustTypeAndChannelType")
    public StandardResponse<CustChannelTypeMappingDTO> getByCustTypeAndChannelType(
            @Parameter(description = "Mã loại khách hàng (CUST_TYPE)", example = "PREPAID", required = true)
            @RequestParam(required = false)
            String custType,
            @Parameter(description = "ID loại kênh (CHANNEL_TYPE_ID)", example = "1", required = true)
            @RequestParam(required = false)
            Long channelTypeId) {
        RequestValidator.checkMaxLength(custType, "custType", 10, "BCCS-ORGANIZATION-VALIDATE-SIZE");
        RequestValidator.checkPattern(custType, "custType", ValidationPatterns.CODE, "BCCS-ORGANIZATION-VALIDATE-PATTERN");
        RequestValidator.checkRange(channelTypeId, "channelTypeId", 0L, 9999999999L, "BCCS-ORGANIZATION-VALIDATE-RANGE");
        return StandardResponses.success(mappingService.getByCustTypeAndChannelType(custType, channelTypeId));
    }
}
