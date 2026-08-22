package com.viettel.bccs.productcatalog.productpackage.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageDTO;
import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageResponse;
import com.viettel.bccs.productcatalog.productpackage.dto.response.SaleServiceAdvanceDTO;
import com.viettel.bccs.productcatalog.productpackage.service.ProductPackageService;
import com.viettel.bccs.productcatalog.utils.RequestValidator;
import com.viettel.bccs.productcatalog.utils.ValidationPatterns;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.viettel.bccs.productcatalog.productpackage.openapi.ProductPackageControllerExamples.*;

@Tag(name = "Product Package", description = "APIs quản lý gói sản phẩm")
@RestController
@RequestMapping("/product-catalog-service/v1/product-package")
@RequiredArgsConstructor
public class ProductPackageController {

    private final ProductPackageService service;

    @Operation(operationId = "findProductPackageById", summary = "Lấy thông tin gói sản phẩm theo ID",
            description = "Tra cứu 1 bản ghi PRODUCT_PACKAGE theo khoá chính PRODUCT_PACKAGE_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_PACKAGE_SINGLE_EXAMPLE)))
    })
    @GetMapping("/findById/{id}")
    public StandardResponse<ProductPackageResponse> findById(
            @Parameter(description = "ID gói sản phẩm (PRODUCT_PACKAGE_ID)", example = "1001", required = true)
            @PathVariable
            Long id) {
        RequestValidator.checkRange(id, "id", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        return StandardResponses.success(service.findById(id));
    }

    @Operation(
            operationId = "getProductPackageByCode",
            summary = "Lấy thông tin gói sản phẩm theo mã gói",
            description = "Truy vấn thông tin gói sản phẩm (product package) theo mã gói. Trả về danh sách vì một mã gói có thể map với nhiều bản ghi (theo telecomServiceId khác nhau). Chỉ trả về bản ghi có `status = 1`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_PACKAGE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByCode/{code}")
    public StandardResponse<List<ProductPackageResponse>> getByCode(
            @Parameter(description = "Mã gói sản phẩm (product_package.code)", example = "PACKAGE_MOBILE_01", required = true)
            @PathVariable
            String code) {
        RequestValidator.requireNotBlank(code, "code", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(code, "code", 50, "BCCS-CATALOG-VALIDATE-SIZE");
        RequestValidator.checkPattern(code, "code", ValidationPatterns.CODE, "BCCS-CATALOG-VALIDATE-PATTERN");
        return StandardResponses.success(service.findByCode(code));
    }

    @Operation(operationId = "getProductPackageByStatus", summary = "Lấy danh sách gói sản phẩm theo trạng thái",
            description = "Truy vấn danh sách PRODUCT_PACKAGE theo cột STATUS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_PACKAGE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByStatus")
    public StandardResponse<List<ProductPackageResponse>> getByStatus(
            @Parameter(description = "Trạng thái (0/1)", example = "1", required = true)
            @RequestParam(required = false)
            String status) {
        RequestValidator.requireNotBlank(status, "status", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(status, "status", 1, "BCCS-CATALOG-VALIDATE-SIZE");
        RequestValidator.checkPattern(status, "status", ValidationPatterns.DIGITS, "BCCS-CATALOG-VALIDATE-PATTERN");
        return StandardResponses.success(service.findByStatus(status));
    }

    @Operation(operationId = "getProductPackageByType", summary = "Lấy danh sách gói sản phẩm theo loại",
            description = "Truy vấn danh sách PRODUCT_PACKAGE theo cột TYPE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_PACKAGE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByType")
    public StandardResponse<List<ProductPackageResponse>> getByType(
            @Parameter(description = "Loại gói sản phẩm (1: hàng hoá, 2: dịch vụ bán hàng)", example = "2", required = true)
            @RequestParam(required = false)
            String type) {
        RequestValidator.requireNotBlank(type, "type", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(type, "type", 1, "BCCS-CATALOG-VALIDATE-SIZE");
        RequestValidator.checkPattern(type, "type", ValidationPatterns.DIGITS, "BCCS-CATALOG-VALIDATE-PATTERN");
        return StandardResponses.success(service.findByType(type));
    }

    @Operation(operationId = "getProductPackageByTelecomServiceId", summary = "Lấy danh sách gói sản phẩm theo ID dịch vụ viễn thông",
            description = "Truy vấn danh sách PRODUCT_PACKAGE theo cột TELECOM_SERVICE_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_PACKAGE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByTelecomServiceId")
    public StandardResponse<List<ProductPackageResponse>> getByTelecomServiceId(
            @Parameter(description = "ID dịch vụ viễn thông (TELECOM_SERVICE_ID)", example = "1", required = true)
            @RequestParam(required = false)
            Long telecomServiceId) {
        RequestValidator.checkRange(telecomServiceId, "telecomServiceId", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        return StandardResponses.success(service.findByTelecomServiceId(telecomServiceId));
    }

    @Operation(operationId = "getPackageCodesByProductOfferTypeCount", summary = "Lấy danh sách mã gói theo số lượng loại mặt hàng",
            description = "Truy vấn danh sách mã gói sản phẩm (product_package.code), loại trừ loại mặt hàng excludeProdOfferType, tuỳ chọn lọc theo số lượng loại mặt hàng (pNumber).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PACKAGE_CODES_EXAMPLE)))
    })
    @GetMapping("/getPackageCodesByProductOfferTypeCount")
    public StandardResponse<List<String>> getPackageCodesByProductOfferTypeCount(
            @Parameter(description = "Loại mặt hàng cần loại trừ", example = "VAS", required = true)
            @RequestParam(required = false)
            String excludeProdOfferType,
            @Parameter(description = "Số lượng loại mặt hàng cần lọc")
            @RequestParam(required = false)
            Integer pNumber) {
        RequestValidator.requireNotBlank(excludeProdOfferType, "excludeProdOfferType", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(excludeProdOfferType, "excludeProdOfferType", 50, "BCCS-CATALOG-VALIDATE-SIZE");
        RequestValidator.checkPattern(excludeProdOfferType, "excludeProdOfferType", ValidationPatterns.FREE_TEXT, "BCCS-CATALOG-VALIDATE-PATTERN");
        RequestValidator.checkRange(pNumber, "pNumber", 0, 999, "BCCS-CATALOG-VALIDATE-RANGE");
        return StandardResponses.success(service.findPackageCodesByProductOfferTypeCount(excludeProdOfferType, pNumber));
    }

    @Operation(operationId = "API_DAUNOI_TT_PRODUCT_011",
            summary = "API lấy thông tin dịch vụ bán hàng",
            description = "API lấy thông tin dịch vụ bán hàng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SALE_SERVICE_ADV_EXAMPLE)))
    })
    @GetMapping("/getSaleServicesAdvBOBySSCode")
    public StandardResponse<SaleServiceAdvanceDTO> getSaleServicesAdvBOBySSCode(
            @Parameter(description = "Mã dịch vụ bán hàng", example = "SALE_SVC_01", required = true)
            @RequestParam(required = false)
            String saleServiceCode) {
        RequestValidator.requireNotBlank(saleServiceCode, "saleServiceCode", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(saleServiceCode, "saleServiceCode", 50, "BCCS-CATALOG-VALIDATE-SIZE");
        RequestValidator.checkPattern(saleServiceCode, "saleServiceCode", ValidationPatterns.CODE, "BCCS-CATALOG-VALIDATE-PATTERN");
        return StandardResponses.success(service.getSaleServicesAdvBOBySSCode(saleServiceCode));
    }

    @Operation(operationId = "API_DAUNOI_TT_PRODUCT_012",
            summary = "Lấy thông tin dịch vụ bán theo mã lý do",
            description = "Lấy thông tin dịch vụ bán theo mã lý do")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SALE_SERVICE_INFO_EXAMPLE)))
    })
    @GetMapping("/getSaleServiceInfo")
    public StandardResponse<ProductPackageDTO> getSaleServiceInfo(
            @Parameter(description = "Id lý do", example = "1", required = true)
            @RequestParam(required = false)
            Long reasonId,
            @Parameter(description = "Mã nhân viên - dùng để kiểm tra tồn kho theo cửa hàng khi loại mặt hàng yêu cầu checkShopStock")
            @RequestParam(required = false)
            String staffCode) {
        RequestValidator.requireNotNull(reasonId, "reasonId", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkRange(reasonId, "reasonId", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        RequestValidator.checkMaxLength(staffCode, "staffCode", 50, "BCCS-CATALOG-VALIDATE-SIZE");
        RequestValidator.checkPattern(staffCode, "staffCode", ValidationPatterns.CODE, "BCCS-CATALOG-VALIDATE-PATTERN");
        return StandardResponses.success(service.getSaleServiceInfo(reasonId, staffCode));
    }
}