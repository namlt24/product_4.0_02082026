package com.viettel.bccs.productcatalog.productpackage.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageDTO;
import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageResponse;
import com.viettel.bccs.productcatalog.productpackage.dto.response.SaleServiceAdvanceDTO;
import com.viettel.bccs.productcatalog.productpackage.service.ProductPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Product Package", description = "APIs quản lý gói sản phẩm")
@RestController
@RequestMapping("/product-catalog-service/v1/product-package")
@RequiredArgsConstructor
public class ProductPackageController {

    private final ProductPackageService service;

    @GetMapping("/findById/{id}")
    public StandardResponse<ProductPackageResponse> findById(@PathVariable Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @Operation(
            operationId = "getProductPackageByCode",
            summary = "Lấy thông tin gói sản phẩm theo mã gói",
            description = "Truy vấn thông tin gói sản phẩm (product package) theo mã gói. Trả về danh sách vì một mã gói có thể map với nhiều bản ghi (theo telecomServiceId khác nhau). Chỉ trả về bản ghi có `status = 1`."
    )
    @GetMapping("/getByCode/{code}")
    public StandardResponse<List<ProductPackageResponse>> getByCode(
            @Parameter(description = "Mã gói sản phẩm (product_package.code)", example = "PACKAGE_MOBILE_01", required = true)
            @PathVariable String code) {
        return StandardResponses.success(service.findByCode(code));
    }

    @GetMapping("/getByStatus")
    public StandardResponse<List<ProductPackageResponse>> getByStatus(@RequestParam String status) {
        return StandardResponses.success(service.findByStatus(status));
    }

    @GetMapping("/getByType")
    public StandardResponse<List<ProductPackageResponse>> getByType(@RequestParam String type) {
        return StandardResponses.success(service.findByType(type));
    }

    @GetMapping("/getByTelecomServiceId")
    public StandardResponse<List<ProductPackageResponse>> getByTelecomServiceId(@RequestParam Long telecomServiceId) {
        return StandardResponses.success(service.findByTelecomServiceId(telecomServiceId));
    }

    @GetMapping("/getPackageCodesByProductOfferTypeCount")
    public StandardResponse<List<String>> getPackageCodesByProductOfferTypeCount(
            @RequestParam String excludeProdOfferType,
            @RequestParam(required = false) Integer pNumber) {
        return StandardResponses.success(service.findPackageCodesByProductOfferTypeCount(excludeProdOfferType, pNumber));
    }

    @Operation(operationId = "API_DAUNOI_TT_PRODUCT_011",
            summary = "API lấy thông tin dịch vụ bán hàng",
            description = "API lấy thông tin dịch vụ bán hàng")
    @GetMapping("/getSaleServicesAdvBOBySSCode")
    public StandardResponse<SaleServiceAdvanceDTO> getSaleServicesAdvBOBySSCode(
            @Parameter(description = "Mã dịch vụ bán hàng") @RequestParam String saleServiceCode) {
        return StandardResponses.success(service.getSaleServicesAdvBOBySSCode(saleServiceCode));
    }

    @Operation(operationId = "API_DAUNOI_TT_PRODUCT_012",
            summary = "Lấy thông tin dịch vụ bán theo mã lý do",
            description = "Lấy thông tin dịch vụ bán theo mã lý do")
    @GetMapping("/getSaleServiceInfo")
    public StandardResponse<ProductPackageDTO> getSaleServiceInfo(
            @Parameter(description = "Id lý do") @RequestParam Long reasonId,
            @Parameter(description = "Mã nhân viên - dùng để kiểm tra tồn kho theo cửa hàng khi loại mặt hàng yêu cầu checkShopStock")
            @RequestParam(required = false) String staffCode) {
        return StandardResponses.success(service.getSaleServiceInfo(reasonId, staffCode));
    }
}