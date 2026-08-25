package com.viettel.bccs.productcatalog.productpackage.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageDTO;
import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageResponse;
import com.viettel.bccs.productcatalog.productpackage.dto.response.SaleServiceAdvanceDTO;
import com.viettel.bccs.productcatalog.productpackage.openapi.ApiFindById;
import com.viettel.bccs.productcatalog.productpackage.openapi.ApiGetByCode;
import com.viettel.bccs.productcatalog.productpackage.openapi.ApiGetByStatus;
import com.viettel.bccs.productcatalog.productpackage.openapi.ApiGetByTelecomServiceId;
import com.viettel.bccs.productcatalog.productpackage.openapi.ApiGetByType;
import com.viettel.bccs.productcatalog.productpackage.openapi.ApiGetPackageCodesByProductOfferTypeCount;
import com.viettel.bccs.productcatalog.productpackage.openapi.ApiGetSaleServiceInfo;
import com.viettel.bccs.productcatalog.productpackage.openapi.ApiGetSaleServicesAdvBOBySSCode;
import com.viettel.bccs.productcatalog.productpackage.service.ProductPackageService;
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

    @ApiFindById
    @GetMapping("/findById/{id}")
    public StandardResponse<ProductPackageResponse> findById(
            @Parameter(description = "ID gói sản phẩm (PRODUCT_PACKAGE_ID)", example = "1001", required = true)
            @PathVariable
            Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @ApiGetByCode
    @GetMapping("/getByCode/{code}")
    public StandardResponse<List<ProductPackageResponse>> getByCode(
            @Parameter(description = "Mã gói sản phẩm (product_package.code)", example = "PACKAGE_MOBILE_01", required = true)
            @PathVariable
            String code) {
        return StandardResponses.success(service.findByCode(code));
    }

    @ApiGetByStatus
    @GetMapping("/getByStatus")
    public StandardResponse<List<ProductPackageResponse>> getByStatus(
            @Parameter(description = "Trạng thái (0/1)", example = "1", required = true)
            @RequestParam(required = false)
            String status) {
        return StandardResponses.success(service.findByStatus(status));
    }

    @ApiGetByType
    @GetMapping("/getByType")
    public StandardResponse<List<ProductPackageResponse>> getByType(
            @Parameter(description = "Loại gói sản phẩm (1: hàng hoá, 2: dịch vụ bán hàng)", example = "2", required = true)
            @RequestParam(required = false)
            String type) {
        return StandardResponses.success(service.findByType(type));
    }

    @ApiGetByTelecomServiceId
    @GetMapping("/getByTelecomServiceId")
    public StandardResponse<List<ProductPackageResponse>> getByTelecomServiceId(
            @Parameter(description = "ID dịch vụ viễn thông (TELECOM_SERVICE_ID)", example = "1", required = true)
            @RequestParam(required = false)
            Long telecomServiceId) {
        return StandardResponses.success(service.findByTelecomServiceId(telecomServiceId));
    }

    @ApiGetPackageCodesByProductOfferTypeCount
    @GetMapping("/getPackageCodesByProductOfferTypeCount")
    public StandardResponse<List<String>> getPackageCodesByProductOfferTypeCount(
            @Parameter(description = "Loại mặt hàng cần loại trừ", example = "VAS", required = true)
            @RequestParam(required = false)
            String excludeProdOfferType,
            @Parameter(description = "Số lượng loại mặt hàng cần lọc")
            @RequestParam(required = false)
            Integer pNumber) {
        return StandardResponses.success(service.findPackageCodesByProductOfferTypeCount(excludeProdOfferType, pNumber));
    }

    @ApiGetSaleServicesAdvBOBySSCode
    @GetMapping("/getSaleServicesAdvBOBySSCode")
    public StandardResponse<SaleServiceAdvanceDTO> getSaleServicesAdvBOBySSCode(
            @Parameter(description = "Mã dịch vụ bán hàng", example = "SALE_SVC_01", required = true)
            @RequestParam(required = false)
            String saleServiceCode) {
        return StandardResponses.success(service.getSaleServicesAdvBOBySSCode(saleServiceCode));
    }

    @ApiGetSaleServiceInfo
    @GetMapping("/getSaleServiceInfo")
    public StandardResponse<ProductPackageDTO> getSaleServiceInfo(
            @Parameter(description = "Id lý do", example = "1", required = true)
            @RequestParam(required = false)
            Long reasonId,
            @Parameter(description = "Mã nhân viên - dùng để kiểm tra tồn kho theo cửa hàng khi loại mặt hàng yêu cầu checkShopStock")
            @RequestParam(required = false)
            String staffCode) {
        return StandardResponses.success(service.getSaleServiceInfo(reasonId, staffCode));
    }
}
