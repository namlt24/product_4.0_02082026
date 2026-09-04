package com.viettel.bccs.productcatalog.productoffercharuse.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingCharacterFullDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.openapi.ApiGetAttributeValue;
import com.viettel.bccs.productcatalog.productoffercharuse.openapi.ApiGetProductOfferCharacter;
import com.viettel.bccs.productcatalog.productoffercharuse.openapi.ApiGetProductSpecCharByOfferingIds;
import com.viettel.bccs.productcatalog.productoffercharuse.service.ProductOfferCharUseService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-catalog-service/v1/product-offer-char-use")
@RequiredArgsConstructor
@Tag(name = "Product Offer Char Use", description = "API quản lý đặc tính sản phẩm")
public class ProductOfferCharUseController {

    private final ProductOfferCharUseService productOfferCharUseService;

    @PostMapping("/getProductSpecCharByOfferingIds")
    @ApiGetProductSpecCharByOfferingIds
    public StandardResponse<Map<Long, List<ProductSpecCharDTO>>> getProductSpecCharByOfferingIds(
            @Parameter(description = "Danh sách offering ID cần tra cứu", example = "[\"500001\", \"500002\"]")
            @RequestBody
            List<String> offeringIds) {
        return StandardResponses.success(productOfferCharUseService.getProductSpecCharByOfferingIds(offeringIds));
    }

    @GetMapping("/getAttributeValue")
    @ApiGetAttributeValue
    public StandardResponse<Optional<String>> getAttributeValue(
            @Parameter(description = "ID sản phẩm", example = "500001", required = true)
            @RequestParam(required = false)
            Long offerId,

            @Parameter(description = "Mã đặc tính (product_spec_char.code)", example = "MONTHLY_FEE", required = true)
            @RequestParam(required = false)
            String attributeName) {
        return StandardResponses.success(productOfferCharUseService.getAttributeValue(offerId, attributeName));
    }

    @GetMapping("/getProductOfferCharacter")
    @ApiGetProductOfferCharacter
    public StandardResponse<List<ProductOfferingCharacterFullDTO>> getProductOfferCharacter(
            @Parameter(description = "ID sản phẩm (product_offering.id)", example = "500001", required = true)
            @RequestParam(required = false)
            Long productOfferingId) {
        return StandardResponses.success(productOfferCharUseService.getProductOfferCharacter(productOfferingId));
    }
}
