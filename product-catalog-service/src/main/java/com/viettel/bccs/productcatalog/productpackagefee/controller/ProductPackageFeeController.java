package com.viettel.bccs.productcatalog.productpackagefee.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productpackagefee.dto.response.ProductPackageFeeResponse;
import com.viettel.bccs.productcatalog.productpackagefee.service.ProductPackageFeeService;
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

    @GetMapping("/findById/{id}")
    public StandardResponse<ProductPackageFeeResponse> findById(@PathVariable Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @GetMapping("/getByProductPackageId/{productPackageId}")
    public StandardResponse<List<ProductPackageFeeResponse>> getByProductPackageId(@PathVariable Long productPackageId) {
        return StandardResponses.success(service.findByProductPackageId(productPackageId));
    }

    @GetMapping("/getByStatus")
    public StandardResponse<List<ProductPackageFeeResponse>> getByStatus(@RequestParam String status) {
        return StandardResponses.success(service.findByStatus(status));
    }

    @GetMapping("/getByProductPackageIdAndStatus")
    public StandardResponse<List<ProductPackageFeeResponse>> getByProductPackageIdAndStatus(
            @RequestParam Long productPackageId,
            @RequestParam String status) {
        return StandardResponses.success(service.findByProductPackageIdAndStatus(productPackageId, status));
    }

    @GetMapping("/getByPricePolicyId")
    public StandardResponse<List<ProductPackageFeeResponse>> getByPricePolicyId(@RequestParam Long pricePolicyId) {
        return StandardResponses.success(service.findByPricePolicyId(pricePolicyId));
    }

    @GetMapping("/getByPriceTypeId")
    public StandardResponse<List<ProductPackageFeeResponse>> getByPriceTypeId(@RequestParam Long priceTypeId) {
        return StandardResponses.success(service.findByPriceTypeId(priceTypeId));
    }
}