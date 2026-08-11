package com.viettel.bccs.organization.custtype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.custtype.dto.CustTypeDTO;
import com.viettel.bccs.organization.custtype.service.CustTypeService;
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
@RequestMapping("/organization-resource-service/v1/cust-type")
@RequiredArgsConstructor
@Validated
@Tag(name = "CustType", description = "Tra cứu loại khách hàng (CUST_TYPE)")
public class CustTypeController {

    private final CustTypeService custTypeService;

    private static final String CUST_TYPE_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000301",
              "requestId": "req-0301",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "custType": "PREPAI",
                "name": "Trả trước",
                "createUser": "admin",
                "createDatetime": "2024-01-01T00:00:00.000+00:00",
                "updateUser": "admin",
                "updateDatetime": "2024-06-01T00:00:00.000+00:00",
                "description": "Khách hàng trả trước",
                "status": "1",
                "groupType": "1",
                "tax": 10,
                "plan": "1",
                "representCust": "0",
                "custTypeId": 1
              }
            }""";

    private static final String CUST_TYPE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000302",
              "requestId": "req-0302",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "custType": "PREPAI",
                  "name": "Trả trước",
                  "createUser": "admin",
                  "createDatetime": "2024-01-01T00:00:00.000+00:00",
                  "updateUser": "admin",
                  "updateDatetime": "2024-06-01T00:00:00.000+00:00",
                  "description": "Khách hàng trả trước",
                  "status": "1",
                  "groupType": "1",
                  "tax": 10,
                  "plan": "1",
                  "representCust": "0",
                  "custTypeId": 1
                }
              ]
            }""";

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
            @Size(min = 1, max = 6, message = "custType tối đa 6 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,6}$", message = "custType chỉ gồm chữ, số, '_' hoặc '-'")
            String custType) {
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
            @RequestParam
            @Min(value = 0, message = "channelTypeId phải >= 0")
            @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
            Long channelTypeId,
            @Parameter(description = "Loại khách hàng (GROUP_TYPE)", example = "1", required = true)
            @RequestParam
            @Size(max = 1, message = "groupType tối đa 1 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,1}$", message = "groupType chỉ gồm chữ, số, '_' hoặc '-'")
            String groupType) {
        return StandardResponses.success(custTypeService.getMappingChannelCustType(channelTypeId, groupType));
    }
}
