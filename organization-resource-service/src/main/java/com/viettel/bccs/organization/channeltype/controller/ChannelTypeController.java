package com.viettel.bccs.organization.channeltype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.channeltype.dto.ChannelTypeDTO;
import com.viettel.bccs.organization.channeltype.service.ChannelTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organization-resource-service/v1/channel-type")
@RequiredArgsConstructor
@Validated
@Tag(name = "ChannelType", description = "Tra cứu loại kênh (CHANNEL_TYPE)")
public class ChannelTypeController {

    private final ChannelTypeService channelTypeService;

    private static final String CHANNEL_TYPE_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000101",
              "requestId": "req-0101",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "channelTypeId": 1,
                "name": "Đại lý",
                "status": "1",
                "objectType": "1",
                "isVtUnit": "1",
                "checkComm": "1",
                "stockType": 1,
                "stockReportTemplate": "TEMPLATE_001",
                "totalDebit": 10,
                "allowAddBatch": 1,
                "suffixObjectCode": "VTST",
                "updateStaffOwnerRole": "ROLE_ADMIN",
                "discountPolicyDefaut": "DISC_DEFAULT",
                "pricePolicyDefaut": "PRICE_DEFAULT",
                "updateBlankCodeRole": "ROLE_BLANK",
                "updateObjectInfoRole": "ROLE_OBJ_INFO",
                "updateShopRole": "ROLE_SHOP",
                "code": "CT01",
                "groupChannelTypeId": 5,
                "groupChannelId": 10,
                "isVhrChannel": 0,
                "isCollChannel": 1,
                "isNotBlankCode": 1,
                "createDatetime": "2024-01-01T00:00:00.000+00:00",
                "createUser": "admin",
                "updateUser": "admin",
                "updateDatetime": "2024-06-01T00:00:00.000+00:00",
                "paymentCode": "PAY001",
                "paymentTail": "TAIL",
                "assignCustStatus": 1,
                "description": "Kênh bán hàng Viettel Store"
              }
            }""";

    @Operation(operationId = "getActiveChannelTypeById", summary = "Lấy loại kênh đang hiệu lực theo ID",
            description = "Tra cứu 1 bản ghi CHANNEL_TYPE đang hiệu lực (status = 1) theo CHANNEL_TYPE_ID (khoá chính).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = CHANNEL_TYPE_SINGLE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy loại kênh với ID tương ứng")
    })
    @GetMapping("/getActiveById/{channelTypeId}")
    public StandardResponse<ChannelTypeDTO> getActiveById(
            @Parameter(description = "ID loại kênh (CHANNEL_TYPE_ID)", example = "1", required = true)
            @PathVariable
            @Min(value = 0, message = "channelTypeId phải >= 0")
            @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
            Long channelTypeId) {
        return StandardResponses.success(channelTypeService.getActiveById(channelTypeId));
    }

}
