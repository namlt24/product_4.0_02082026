package com.viettel.bccs.policy.mapping.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapping.service.MappingService;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Mapping", description = "APIs quản lý mapping dịch vụ bán hàng")
@RestController
@RequestMapping("/product-policy-service/v1/mapping")
@RequiredArgsConstructor
@Validated
public class MappingController {

    private final MappingService service;

    private static final String SALE_SERVICE_CODE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": ["SS001", "SS002"]
            }""";

    private static final String REASON_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": []
            }""";

    @Operation(operationId = "findSaleServiceCodeByReason", summary = "Tìm mã dịch vụ bán hàng theo lý do",
            description = "Trả về danh sách SALE_SERVICE_CODE trong bảng MAPPING khớp với REASON_ID truyền vào.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SALE_SERVICE_CODE_LIST_EXAMPLE)))
    })
    @GetMapping("/findSaleServiceCodeByReason/{reasonId}")
    public StandardResponse<List<String>> findSaleServiceCodeByReason(
            @Parameter(description = "Id lý do", example = "1", required = true)
            @PathVariable
            @Min(value = 0, message = "reasonId phải >= 0")
            @Max(value = 9999999999L, message = "reasonId vượt quá độ dài cột (precision 10)")
            Long reasonId) {
        return StandardResponses.success(service.findSaleServiceCodeByReason(reasonId));
    }

    @Operation(operationId = "getMappingReasonProductOfferPrice", summary = "Lấy danh sách lý do (reason) mapping theo gói sản phẩm (sale service), phục vụ getPriceInServices",
            description = "Trả về danh sách REASON khớp với gói sản phẩm (product package / sale service) truyền vào.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = REASON_LIST_EXAMPLE)))
    })
    @GetMapping("/getMappingReasonProductOfferPrice/{productPackageId}")
    public StandardResponse<List<ReasonResponse>> getMappingReasonProductOfferPrice(
            @Parameter(description = "Id gói sản phẩm (product package / sale service)", example = "1", required = true)
            @PathVariable
            @Min(value = 0, message = "productPackageId phải >= 0")
            @Max(value = 9999999999L, message = "productPackageId vượt quá độ dài cột (precision 10)")
            Long productPackageId) {
        return StandardResponses.success(service.getMappingReasonProductOfferPrice(productPackageId));
    }
}
