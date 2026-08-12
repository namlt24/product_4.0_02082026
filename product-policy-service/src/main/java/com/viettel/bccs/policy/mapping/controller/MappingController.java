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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.viettel.bccs.policy.mapping.openapi.MappingControllerExamples.*;

@Tag(name = "Mapping", description = "APIs quản lý mapping dịch vụ bán hàng")
@RestController
@RequestMapping("/product-policy-service/v1/mapping")
@RequiredArgsConstructor
@Validated
public class MappingController {

    private final MappingService service;

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

    @Operation(operationId = "getSaleServiceCode", summary = "Tìm mã dịch vụ bán hàng theo dịch vụ viễn thông, lý do, mã gói cước, mã hành động",
            description = "Trả về SALE_SERVICE_CODE trong bảng MAPPING khớp reasonId, telecomServiceId (hoặc mapping không ràng buộc dịch vụ), productCode (hoặc mapping không ràng buộc gói cước) và actionCode (qua reason_type). Trả về null nếu reasonId null/0 hoặc không tìm thấy. Phục vụ product-catalog-service (API getListStockTypeWS).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SALE_SERVICE_CODE_EXAMPLE)))
    })
    @GetMapping("/getSaleServiceCode")
    public StandardResponse<String> getSaleServiceCode(
            @Parameter(description = "ID dịch vụ viễn thông", example = "1")
            @RequestParam(required = false)
            @Min(value = 0, message = "telecomServiceId phải >= 0")
            @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
            Long telecomServiceId,
            @Parameter(description = "Id lý do", example = "1", required = true)
            @RequestParam
            @Min(value = 0, message = "reasonId phải >= 0")
            @Max(value = 9999999999L, message = "reasonId vượt quá độ dài cột (precision 10)")
            Long reasonId,
            @Parameter(description = "Mã gói cước", example = "POBAS")
            @RequestParam(required = false)
            @Size(max = 50, message = "productCode tối đa 50 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
            String productCode,
            @Parameter(description = "Mã hành động", example = "00")
            @RequestParam(required = false)
            @Size(max = 10, message = "actionCode tối đa 10 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "actionCode chỉ gồm chữ, số, '_' hoặc '-'")
            String actionCode) {
        return StandardResponses.success(service.getSaleServiceCode(telecomServiceId, reasonId, productCode, actionCode));
    }
}
