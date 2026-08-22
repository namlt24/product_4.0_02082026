package com.viettel.bccs.productcatalog.productpackagefee.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productpackagefee.dto.response.ProductPackageFeeResponse;
import com.viettel.bccs.productcatalog.productpackagefee.service.ProductPackageFeeService;
import com.viettel.bccs.productcatalog.utils.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.viettel.bccs.productcatalog.productpackagefee.openapi.ProductPackageFeeControllerExamples.*;

@RestController
@RequestMapping("/product-catalog-service/v1/product-package-fee")
@RequiredArgsConstructor
public class ProductPackageFeeController {

    private final ProductPackageFeeService service;

    @Operation(operationId = "findProductPackageFeeById", summary = "Lấy thông tin phí gói sản phẩm theo ID",
            description = "Tra cứu 1 bản ghi PRODUCT_PACKAGE_FEE theo khoá chính PRODUCT_PACKAGE_FEE_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = FEE_SINGLE_EXAMPLE)))
    })
    @GetMapping("/findById/{id}")
    public StandardResponse<ProductPackageFeeResponse> findById(
            @Parameter(description = "ID phí gói sản phẩm (PRODUCT_PACKAGE_FEE_ID)", example = "1", required = true)
            @PathVariable
            Long id) {
        RequestValidator.checkRange(id, "id", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        return StandardResponses.success(service.findById(id));
    }

    @Operation(operationId = "getProductPackageFeeByProductPackageId", summary = "Lấy danh sách phí theo ID gói sản phẩm",
            description = "Truy vấn danh sách PRODUCT_PACKAGE_FEE theo cột PRODUCT_PACKAGE_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = FEE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByProductPackageId/{productPackageId}")
    public StandardResponse<List<ProductPackageFeeResponse>> getByProductPackageId(
            @Parameter(description = "ID gói sản phẩm (PRODUCT_PACKAGE_ID)", example = "1001", required = true)
            @PathVariable
            Long productPackageId) {
        RequestValidator.checkRange(productPackageId, "productPackageId", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        return StandardResponses.success(service.findByProductPackageId(productPackageId));
    }

    @Operation(operationId = "getProductPackageFeeByStatus", summary = "Lấy danh sách phí gói sản phẩm theo trạng thái",
            description = "Truy vấn danh sách PRODUCT_PACKAGE_FEE theo cột STATUS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = FEE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByStatus")
    public StandardResponse<List<ProductPackageFeeResponse>> getByStatus(
            @Parameter(description = "Trạng thái (0/1)", example = "1", required = true)
            @RequestParam(required = false)
            String status) {
        RequestValidator.requireNotBlank(status, "status", "BCCS-CATALOG-VALIDATE-REQUIRED");
        return StandardResponses.success(service.findByStatus(status));
    }

    @Operation(operationId = "getProductPackageFeeByProductPackageIdAndStatus", summary = "Lấy danh sách phí theo ID gói sản phẩm và trạng thái",
            description = "Truy vấn danh sách PRODUCT_PACKAGE_FEE theo cột PRODUCT_PACKAGE_ID và STATUS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = FEE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByProductPackageIdAndStatus")
    public StandardResponse<List<ProductPackageFeeResponse>> getByProductPackageIdAndStatus(
            @Parameter(description = "ID gói sản phẩm (PRODUCT_PACKAGE_ID)", example = "1001", required = true)
            @RequestParam(required = false)
            Long productPackageId,
            @Parameter(description = "Trạng thái (0/1)", example = "1", required = true)
            @RequestParam(required = false)
            String status) {
        RequestValidator.checkRange(productPackageId, "productPackageId", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        RequestValidator.requireNotBlank(status, "status", "BCCS-CATALOG-VALIDATE-REQUIRED");
        return StandardResponses.success(service.findByProductPackageIdAndStatus(productPackageId, status));
    }

    @Operation(operationId = "getProductPackageFeeByPricePolicyId", summary = "Lấy danh sách phí theo ID chính sách giá",
            description = "Truy vấn danh sách PRODUCT_PACKAGE_FEE theo cột PRICE_POLICY_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = FEE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByPricePolicyId")
    public StandardResponse<List<ProductPackageFeeResponse>> getByPricePolicyId(
            @Parameter(description = "ID chính sách giá (PRICE_POLICY_ID)", example = "1", required = true)
            @RequestParam(required = false)
            Long pricePolicyId) {
        RequestValidator.checkRange(pricePolicyId, "pricePolicyId", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        return StandardResponses.success(service.findByPricePolicyId(pricePolicyId));
    }

    @Operation(operationId = "getProductPackageFeeByPriceTypeId", summary = "Lấy danh sách phí theo ID loại giá",
            description = "Truy vấn danh sách PRODUCT_PACKAGE_FEE theo cột PRICE_TYPE_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = FEE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByPriceTypeId")
    public StandardResponse<List<ProductPackageFeeResponse>> getByPriceTypeId(
            @Parameter(description = "ID loại giá (PRICE_TYPE_ID)", example = "1", required = true)
            @RequestParam(required = false)
            Long priceTypeId) {
        RequestValidator.checkRange(priceTypeId, "priceTypeId", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        return StandardResponses.success(service.findByPriceTypeId(priceTypeId));
    }
}