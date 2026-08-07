package com.viettel.bccs.productcatalog.productofferprice.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceDTO;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceResponse;
import com.viettel.bccs.productcatalog.productofferprice.service.ProductOfferPriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-catalog-service/v1/productofferprice")
@RequiredArgsConstructor
@Tag(name = "Product Offer Price", description = "API quản lý giá bán thiết bị")
public class ProductOfferPriceController {

    private final ProductOfferPriceService productOfferPriceService;

    @GetMapping("/getById")
    @Operation(
            operationId = "getProductOfferPriceById",
            summary = "Lấy thông tin giá bán thiết bị theo ID",
            description = "Truy vấn chi tiết giá bán thiết bị (PRODUCT_OFFER_PRICE) theo productOfferPriceId. Trả về đầy đủ thông tin bao gồm giá, VAT, thời gian hiệu lực, chính sách giá và các trường audit.")
    public StandardResponse<ProductOfferPriceDTO> getById(
            @Parameter(description = "ID giá bán thiết bị", example = "12345", required = true)
            @RequestParam Long prodOfferPriceId) {
        return StandardResponses.success(productOfferPriceService.getById(prodOfferPriceId));
    }

    @PostMapping("/getPriceInServicesForPCCC")
    @Operation(
            operationId = "getPriceInServicesForPCCC",
            summary = "Lấy danh sách giá bán thiết bị phục vụ quản lý cước PCCC",
            description = "Truy vấn danh sách giá bán thiết bị theo các tiêu chí: ID/Code gói sản phẩm, loại sản phẩm, ID sản phẩm và chính sách giá. Phục vụ cho nghiệp vụ quản lý cước PCCC.")
    public StandardResponse<List<ProductOfferPriceDTO>> getPriceInServicesForPCCC(
            @Parameter(description = "ID gói sản phẩm", example = "123")
            @RequestParam(required = false) Long productPackageId,

            @Parameter(description = "Mã gói sản phẩm", example = "PKG_001")
            @RequestParam(required = false) String productPackageCode,

            @Parameter(description = "ID loại sản phẩm", example = "1")
            @RequestParam(required = false) Long productOfferType,

            @Parameter(description = "ID sản phẩm", example = "456")
            @RequestParam(required = false) Long productOfferId,

            @Parameter(description = "ID chính sách giá", example = "7")
            @RequestParam(required = false) Long pricePolicy) {
        return StandardResponses.success(productOfferPriceService.getPriceInServicesForPCCC(
                productPackageId, productPackageCode, productOfferType, productOfferId, pricePolicy));
    }


}