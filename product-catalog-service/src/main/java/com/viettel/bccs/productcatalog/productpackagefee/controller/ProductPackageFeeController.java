package com.viettel.bccs.productcatalog.productpackagefee.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productpackagefee.dto.response.ProductPackageFeeResponse;
import com.viettel.bccs.productcatalog.productpackagefee.openapi.ApiFindById;
import com.viettel.bccs.productcatalog.productpackagefee.openapi.ApiGetByPricePolicyId;
import com.viettel.bccs.productcatalog.productpackagefee.openapi.ApiGetByPriceTypeId;
import com.viettel.bccs.productcatalog.productpackagefee.openapi.ApiGetByProductPackageId;
import com.viettel.bccs.productcatalog.productpackagefee.openapi.ApiGetByProductPackageIdAndStatus;
import com.viettel.bccs.productcatalog.productpackagefee.openapi.ApiGetByStatus;
import com.viettel.bccs.productcatalog.productpackagefee.service.ProductPackageFeeService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product-catalog-service/v1/product-package-fee")
@RequiredArgsConstructor
public class ProductPackageFeeController {

    private final ProductPackageFeeService service;

    @ApiFindById
    @GetMapping("/findById/{id}")
    public StandardResponse<ProductPackageFeeResponse> findById(
            @Parameter(description = "ID phí gói sản phẩm (PRODUCT_PACKAGE_FEE_ID)", example = "1", required = true)
            @PathVariable
            Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @ApiGetByProductPackageId
    @GetMapping("/getByProductPackageId/{productPackageId}")
    public StandardResponse<List<ProductPackageFeeResponse>> getByProductPackageId(
            @Parameter(description = "ID gói sản phẩm (PRODUCT_PACKAGE_ID)", example = "1001", required = true)
            @PathVariable
            Long productPackageId) {
        return StandardResponses.success(service.findByProductPackageId(productPackageId));
    }

    @ApiGetByStatus
    @GetMapping("/getByStatus")
    public StandardResponse<List<ProductPackageFeeResponse>> getByStatus(
            @Parameter(description = "Trạng thái (0/1)", example = "1", required = true)
            @RequestParam(required = false)
            String status) {
        return StandardResponses.success(service.findByStatus(status));
    }

    @ApiGetByProductPackageIdAndStatus
    @GetMapping("/getByProductPackageIdAndStatus")
    public StandardResponse<List<ProductPackageFeeResponse>> getByProductPackageIdAndStatus(
            @Parameter(description = "ID gói sản phẩm (PRODUCT_PACKAGE_ID)", example = "1001", required = true)
            @RequestParam(required = false)
            Long productPackageId,
            @Parameter(description = "Trạng thái (0/1)", example = "1", required = true)
            @RequestParam(required = false)
            String status) {
        return StandardResponses.success(service.findByProductPackageIdAndStatus(productPackageId, status));
    }

    @ApiGetByPricePolicyId
    @GetMapping("/getByPricePolicyId")
    public StandardResponse<List<ProductPackageFeeResponse>> getByPricePolicyId(
            @Parameter(description = "ID chính sách giá (PRICE_POLICY_ID)", example = "1", required = true)
            @RequestParam(required = false)
            Long pricePolicyId) {
        return StandardResponses.success(service.findByPricePolicyId(pricePolicyId));
    }

    @ApiGetByPriceTypeId
    @GetMapping("/getByPriceTypeId")
    public StandardResponse<List<ProductPackageFeeResponse>> getByPriceTypeId(
            @Parameter(description = "ID loại giá (PRICE_TYPE_ID)", example = "1", required = true)
            @RequestParam(required = false)
            Long priceTypeId) {
        return StandardResponses.success(service.findByPriceTypeId(priceTypeId));
    }
}
