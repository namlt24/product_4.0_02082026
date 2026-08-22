package com.viettel.bccs.productcatalog.productofferprice.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceDTO;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceResponse;
import com.viettel.bccs.productcatalog.productofferprice.service.ProductOfferPriceService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.viettel.bccs.productcatalog.productofferprice.openapi.ProductOfferPriceControllerExamples.*;

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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRICE_DTO_EXAMPLE)))
    })
    public StandardResponse<ProductOfferPriceDTO> getById(
            @Parameter(description = "ID giá bán thiết bị", example = "12345", required = true)
            @RequestParam(required = false)
            Long prodOfferPriceId) {
        RequestValidator.requireNotNull(prodOfferPriceId, "prodOfferPriceId", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkRange(prodOfferPriceId, "prodOfferPriceId", 1L, 999999999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        return StandardResponses.success(productOfferPriceService.getById(prodOfferPriceId));
    }

    @PostMapping("/getPriceInServicesForPCCC")
    @Operation(
            operationId = "getPriceInServicesForPCCC",
            summary = "Lấy danh sách giá bán thiết bị phục vụ quản lý cước PCCC",
            description = "Truy vấn danh sách giá bán thiết bị theo các tiêu chí: ID/Code gói sản phẩm, loại sản phẩm, ID sản phẩm và chính sách giá. Phục vụ cho nghiệp vụ quản lý cước PCCC.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRICE_DTO_LIST_EXAMPLE)))
    })
    public StandardResponse<List<ProductOfferPriceDTO>> getPriceInServicesForPCCC(
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
        RequestValidator.checkRange(productPackageId, "productPackageId", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        RequestValidator.checkMaxLength(productPackageCode, "productPackageCode", 50, "BCCS-CATALOG-VALIDATE-SIZE");
        RequestValidator.checkPattern(productPackageCode, "productPackageCode", ValidationPatterns.CODE, "BCCS-CATALOG-VALIDATE-PATTERN");
        RequestValidator.checkRange(productOfferType, "productOfferType", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        RequestValidator.checkRange(productOfferId, "productOfferId", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        RequestValidator.checkRange(pricePolicy, "pricePolicy", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        return StandardResponses.success(productOfferPriceService.getPriceInServicesForPCCC(
                productPackageId, productPackageCode, productOfferType, productOfferId, pricePolicy));
    }

    @GetMapping("/getPriceInServices")
    @Operation(
            operationId = "getPriceInServices",
            summary = "Lấy danh sách giá bán thiết bị trong gói dịch vụ",
            description = "Truy vấn danh sách giá bán thiết bị (ProductOfferPrice) theo các tiêu chí: ID/Code gói sản phẩm, loại sản phẩm, ID sản phẩm và chính sách giá. " +
                    "Hỗ trợ tính giá thiết bị CAM theo loại (indoor/outdoor) và giá thiết bị thông thường. " +
                    "Kết quả bao gồm thông tin giá, tên sản phẩm, và giá thiết bị khuyến mãi.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRICE_RESPONSE_LIST_EXAMPLE)))
    })
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
        RequestValidator.checkRange(productPackageId, "productPackageId", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        RequestValidator.checkMaxLength(productPackageCode, "productPackageCode", 50, "BCCS-CATALOG-VALIDATE-SIZE");
        RequestValidator.checkPattern(productPackageCode, "productPackageCode", ValidationPatterns.CODE, "BCCS-CATALOG-VALIDATE-PATTERN");
        RequestValidator.checkRange(productOfferType, "productOfferType", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        RequestValidator.checkRange(productOfferId, "productOfferId", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        RequestValidator.checkRange(pricePolicy, "pricePolicy", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        return StandardResponses.success(productOfferPriceService.getPriceInServices(
                productPackageId, productPackageCode, productOfferType, productOfferId, pricePolicy));
    }

    @GetMapping("/getPriceByTypePolicy")
    @Operation(
            operationId = "getPriceByTypePolicy",
            summary = "Lấy danh sách giá bán theo loại giá và chính sách giá",
            description = "Truy vấn danh sách giá bán thiết bị (PRODUCT_OFFER_PRICE) đang active của 1 sản phẩm theo priceTypeId và pricePolicy, chỉ lấy bản ghi còn hiệu lực theo ngày hiện tại (EFFECT_DATETIME/EXPIRE_DATETIME). Kết quả sắp xếp tăng dần theo giá.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRICE_DTO_LIST_EXAMPLE)))
    })
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
        RequestValidator.requireNotNull(productOfferId, "productOfferId", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkRange(productOfferId, "productOfferId", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        RequestValidator.requireNotNull(priceTypeId, "priceTypeId", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkRange(priceTypeId, "priceTypeId", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        RequestValidator.requireNotNull(pricePolicy, "pricePolicy", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkRange(pricePolicy, "pricePolicy", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        return StandardResponses.success(productOfferPriceService.getPriceByTypePolicy(productOfferId, priceTypeId, pricePolicy));
    }

}
