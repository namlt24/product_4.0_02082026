package com.viettel.bccs.productcatalog.productoffercharuse.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.service.ProductOfferCharUseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/product-catalog-service/v1/product-offer-char-use")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Offer Char Use", description = "API quản lý đặc tính sản phẩm")
public class ProductOfferCharUseController {

    private final ProductOfferCharUseService productOfferCharUseService;

    private static final String SPEC_CHAR_MAP_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "500001": [
                  {
                    "productSpecCharId": 12345,
                    "name": "Giá cước tháng",
                    "code": "MONTHLY_FEE",
                    "charType": "PRICE_PLAN",
                    "status": "1",
                    "productOfferingId": 500001
                  }
                ]
              }
            }""";

    private static final String ATTRIBUTE_VALUE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": "50000"
            }""";

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
            @Size(min = 1, max = 1000, message = "offeringIds phải có từ 1 đến 1000 phần tử")
            List<String> offeringIds) {
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
            @RequestParam
            @Min(value = 1, message = "offerId phải >= 1")
            @Max(value = 9999999999L, message = "offerId vượt quá độ dài cột (precision 10)")
            Long offerId,

            @Parameter(description = "Mã đặc tính (product_spec_char.code)", example = "MONTHLY_FEE", required = true)
            @RequestParam
            @Size(min = 1, max = 200, message = "attributeName tối đa 200 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,200}$", message = "attributeName chỉ gồm chữ, số, '_' hoặc '-'")
            String attributeName) {
        return StandardResponses.success(productOfferCharUseService.getAttributeValue(offerId, attributeName));
    }
}
