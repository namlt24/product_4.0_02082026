package com.viettel.bccs.productcatalog.productoffercharuse.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.service.ProductOfferCharUseService;
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
import java.util.Map;
import java.util.Optional;

import static com.viettel.bccs.productcatalog.productoffercharuse.openapi.ProductOfferCharUseControllerExamples.*;

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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SPEC_CHAR_MAP_EXAMPLE)))
    })
    public StandardResponse<Map<Long, List<ProductSpecCharDTO>>> getProductSpecCharByOfferingIds(
            @Parameter(description = "Danh sách offering ID cần tra cứu", example = "[\"500001\", \"500002\"]")
            @RequestBody
            List<String> offeringIds) {
        RequestValidator.requireNotEmpty(offeringIds, "offeringIds", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkSize(offeringIds, "offeringIds", 1000, "BCCS-CATALOG-VALIDATE-SIZE");
        return StandardResponses.success(productOfferCharUseService.getProductSpecCharByOfferingIds(offeringIds));
    }

    @GetMapping("/getAttributeValue")
    @Operation(operationId = "API_DAUNOI_TT_PRODUCT_020",
            summary = "Lấy giá trị thuộc tính sản phẩm",
            description = "Truy vấn giá trị của một đặc tính (attribute) theo offerId (product_offering.id) và attributeName (product_spec_char.code). Ưu tiên trả về SPECIFIC_VALUE, fallback về VALUE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = ATTRIBUTE_VALUE_EXAMPLE)))
    })
    public StandardResponse<Optional<String>> getAttributeValue(
            @Parameter(description = "ID sản phẩm", example = "500001", required = true)
            @RequestParam(required = false)
            Long offerId,

            @Parameter(description = "Mã đặc tính (product_spec_char.code)", example = "MONTHLY_FEE", required = true)
            @RequestParam(required = false)
            String attributeName) {
        RequestValidator.requireNotNull(offerId, "offerId", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkRange(offerId, "offerId", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        RequestValidator.requireNotBlank(attributeName, "attributeName", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(attributeName, "attributeName", 200, "BCCS-CATALOG-VALIDATE-SIZE");
        RequestValidator.checkPattern(attributeName, "attributeName", ValidationPatterns.CODE, "BCCS-CATALOG-VALIDATE-PATTERN");
        return StandardResponses.success(productOfferCharUseService.getAttributeValue(offerId, attributeName));
    }
}
