package com.viettel.bccs.productcatalog.productoffercharuse.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.service.ProductOfferCharUseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/product-catalog-service/v1/product-offer-char-use")
@RequiredArgsConstructor
@Tag(name = "Product Offer Char Use", description = "API quản lý đặc tính sản phẩm")
public class ProductOfferCharUseController {

    private final ProductOfferCharUseService productOfferCharUseService;

    @PostMapping("/getProductSpecCharByOfferingIds")
    @Operation(operationId = "getProductSpecCharByOfferingIds",
            summary = "Lấy đặc tính sản phẩm theo danh sách offering IDs",
            description = "Batch fetch đặc tính sản phẩm cho nhiều offering IDs trong 1 query, trả về Map<offeringId, list specChar>")
    public StandardResponse<Map<Long, List<ProductSpecCharDTO>>> getProductSpecCharByOfferingIds(
            @RequestBody List<String> offeringIds) {
        return StandardResponses.success(productOfferCharUseService.getProductSpecCharByOfferingIds(offeringIds));
    }

    @GetMapping("/getAttributeValue")
    @Operation(operationId = "API_DAUNOI_TT_PRODUCT_020",
            summary = "Lấy giá trị thuộc tính sản phẩm",
            description = "Truy vấn giá trị của một đặc tính (attribute) theo offerId (product_offering.id) và attributeName (product_spec_char.code). Ưu tiên trả về SPECIFIC_VALUE, fallback về VALUE.")
    public StandardResponse<Optional<String>> getAttributeValue(
            @RequestParam Long offerId,
            @RequestParam String attributeName) {
        return StandardResponses.success(productOfferCharUseService.getAttributeValue(offerId, attributeName));
    }
}