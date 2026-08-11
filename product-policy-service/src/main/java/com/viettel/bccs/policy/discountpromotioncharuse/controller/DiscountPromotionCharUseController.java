package com.viettel.bccs.policy.discountpromotioncharuse.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.discountpromotioncharuse.dto.response.DiscountPromotionCharUseResponse;
import com.viettel.bccs.policy.discountpromotioncharuse.service.DiscountPromotionCharUseService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DiscountPromotionCharUse", description = "APIs quản lý thuộc tính sử dụng của khuyến mãi giảm giá")
@RestController
@RequestMapping("/product-policy-service/v1/discount-promotion-char-use")
@RequiredArgsConstructor
@Validated
public class DiscountPromotionCharUseController {

    private final DiscountPromotionCharUseService service;

    private static final String FIND_BY_ID_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "discountPromotionCharUseId": 1,
                "discountPromotionId": 1,
                "productSpecCharValueId": 1,
                "productSpecCharId": 1,
                "createUser": "system",
                "createDatetime": "2026-08-01T00:00:00.000+00:00",
                "updateUser": null,
                "updateDatetime": null,
                "status": "1",
                "specificValue": null,
                "effectDatetime": "2026-08-01T00:00:00.000+00:00",
                "expireDatetime": null,
                "limited": null,
                "note": null
              }
            }""";

    @Operation(operationId = "findDiscountPromotionCharUseById", summary = "Lấy thuộc tính sử dụng khuyến mãi theo id",
            description = "Tra cứu 1 bản ghi DISCOUNT_PROMOTION_CHAR_USE theo DISCOUNT_PROMOTION_CHAR_USE_ID (khoá chính).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = FIND_BY_ID_EXAMPLE)))
    })
    @GetMapping("/findById/{id}")
    public StandardResponse<DiscountPromotionCharUseResponse> findById(
            @Parameter(description = "Id thuộc tính sử dụng khuyến mãi (DISCOUNT_PROMOTION_CHAR_USE_ID)", example = "1", required = true)
            @PathVariable
            @Min(value = 0, message = "id phải >= 0")
            @Max(value = 9999999999L, message = "id vượt quá độ dài cột (precision 10)")
            Long id) {
        return StandardResponses.success(service.findById(id));
    }
}
