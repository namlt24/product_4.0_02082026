package com.viettel.bccs.productcatalog.productofferprice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.PledgePriceResponse;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceDTO;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceResponse;
import com.viettel.bccs.productcatalog.productofferprice.openapi.ApiGetById;
import com.viettel.bccs.productcatalog.productofferprice.openapi.ApiGetPledgePriceInfoByOfferId;
import com.viettel.bccs.productcatalog.productofferprice.openapi.ApiGetPriceByTypePolicy;
import com.viettel.bccs.productcatalog.productofferprice.openapi.ApiGetPriceInServices;
import com.viettel.bccs.productcatalog.productofferprice.openapi.ApiGetPriceInServicesForPccc;
import com.viettel.bccs.productcatalog.productofferprice.service.ProductOfferPriceService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-catalog-service/v1/productofferprice")
@RequiredArgsConstructor
@Tag(name = "Product Offer Price", description = "API quản lý giá bán thiết bị")
public class ProductOfferPriceController {

    private final ProductOfferPriceService productOfferPriceService;

    @GetMapping("/getPledgePriceInfoByOfferId")
    @ApiGetPledgePriceInfoByOfferId
    public StandardResponse<List<PledgePriceResponse>> getPledgePriceInfoByOfferId(
            @Parameter(description = "ID mặt hàng (PRODUCT_OFFERING_ID)", example = "456", required = true)
            @RequestParam(required = false)
            Long productOfferingId) {
        return StandardResponses.success(productOfferPriceService.getPledgePriceInfoByOfferId(productOfferingId));
    }

    @GetMapping("/getById")
    @ApiGetById
    public StandardResponse<ProductOfferPriceDTO> getById(
            @Parameter(description = "ID giá bán thiết bị", example = "12345", required = true)
            @RequestParam(required = false)
            Long prodOfferPriceId) {
        return StandardResponses.success(productOfferPriceService.getById(prodOfferPriceId));
    }

    @PostMapping("/getPriceInServicesForPccc")
    @ApiGetPriceInServicesForPccc
    public StandardResponse<List<ProductOfferPriceDTO>> getPriceInServicesForPccc(
            @Parameter(description = "ID gói sản phẩm", example = "123")
            @RequestParam(required = false)
            Long productPackageId,

            @Parameter(description = "Mã gói sản phẩm", example = "PKG_001")
            @RequestParam(required = false)
            String productPackageCode,

            @Parameter(description = "ID loại sản phẩm", example = "1")
            @RequestParam(required = false)
            Long productOfferType,

            @Parameter(description = "ID sản phẩm", example = "456")
            @RequestParam(required = false)
            Long productOfferId,

            @Parameter(description = "ID chính sách giá", example = "7")
            @RequestParam(required = false)
            Long pricePolicy) {
        return StandardResponses.success(productOfferPriceService.getPriceInServicesForPccc(
                productPackageId, productPackageCode, productOfferType, productOfferId, pricePolicy));
    }

    @GetMapping("/getPriceInServices")
    @ApiGetPriceInServices
    public StandardResponse<List<ProductOfferPriceResponse>> getPriceInServices(
            @Parameter(description = "ID gói sản phẩm", example = "123")
            @RequestParam(required = false)
            Long productPackageId,

            @Parameter(description = "Mã gói sản phẩm", example = "PKG_001")
            @RequestParam(required = false)
            String productPackageCode,

            @Parameter(description = "ID loại sản phẩm", example = "1")
            @RequestParam(required = false)
            Long productOfferType,

            @Parameter(description = "ID sản phẩm", example = "456")
            @RequestParam(required = false)
            Long productOfferId,

            @Parameter(description = "ID chính sách giá", example = "7")
            @RequestParam(required = false)
            Long pricePolicy) {
        return StandardResponses.success(productOfferPriceService.getPriceInServices(
                productPackageId, productPackageCode, productOfferType, productOfferId, pricePolicy));
    }

    @GetMapping("/getPriceByTypePolicy")
    @ApiGetPriceByTypePolicy
    public StandardResponse<List<ProductOfferPriceDTO>> getPriceByTypePolicy(
            @Parameter(description = "ID sản phẩm", example = "456", required = true)
            @RequestParam(required = false)
            Long productOfferId,

            @Parameter(description = "ID loại giá", example = "1", required = true)
            @RequestParam(required = false)
            Long priceTypeId,

            @Parameter(description = "ID chính sách giá", example = "7", required = true)
            @RequestParam(required = false)
            Long pricePolicy) {
        return 
                StandardResponses.success(productOfferPriceService.getPriceByTypePolicy(productOfferId, priceTypeId,
                    pricePolicy));
    }
    //
}
