package com.viettel.bccs.organization.custchanneltype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.custchanneltype.dto.CustChannelTypeMappingDTO;
import com.viettel.bccs.organization.custchanneltype.service.CustChannelTypeMappingService;
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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization-resource-service/v1/cust-channel-type-mapping")
@RequiredArgsConstructor
@Validated
@Tag(name = "CustChannelTypeMapping", description = "Tra cứu mapping loại khách hàng - loại kênh (CUST_CHANNEL_TYPE_MAPPING)")
public class CustChannelTypeMappingController {

    private final CustChannelTypeMappingService mappingService;

    private static final String MAPPING_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000201",
              "requestId": "req-0201",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "custChannelTypeMapId": 1,
                  "custType": "PREPAID",
                  "channelTypeId": 1,
                  "status": "1",
                  "createUser": "admin",
                  "createDatetime": "2024-01-01T00:00:00.000+00:00",
                  "updateUser": "admin",
                  "updateDatetime": "2024-06-01T00:00:00.000+00:00"
                }
              ]
            }""";

    private static final String MAPPING_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000202",
              "requestId": "req-0202",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "custChannelTypeMapId": 1,
                "custType": "PREPAID",
                "channelTypeId": 1,
                "status": "1",
                "createUser": "admin",
                "createDatetime": "2024-01-01T00:00:00.000+00:00",
                "updateUser": "admin",
                "updateDatetime": "2024-06-01T00:00:00.000+00:00"
              }
            }""";

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
            @Min(value = 0, message = "channelTypeId phải >= 0")
            @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
            Long channelTypeId) {
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
            @RequestParam
            @Size(max = 10, message = "custType tối đa 10 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "custType chỉ gồm chữ, số, '_' hoặc '-'")
            String custType,
            @Parameter(description = "ID loại kênh (CHANNEL_TYPE_ID)", example = "1", required = true)
            @RequestParam
            @Min(value = 0, message = "channelTypeId phải >= 0")
            @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
            Long channelTypeId) {
        return StandardResponses.success(mappingService.getByCustTypeAndChannelType(custType, channelTypeId));
    }
}
