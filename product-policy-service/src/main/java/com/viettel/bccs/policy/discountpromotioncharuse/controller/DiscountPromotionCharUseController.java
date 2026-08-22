package com.viettel.bccs.policy.discountpromotioncharuse.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.discountpromotioncharuse.dto.response.DiscountPromotionCharUseResponse;
import com.viettel.bccs.policy.discountpromotioncharuse.service.DiscountPromotionCharUseService;
import com.viettel.bccs.policy.utils.RequestValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.viettel.bccs.policy.discountpromotioncharuse.openapi.DiscountPromotionCharUseControllerExamples.*;

@Tag(name = "DiscountPromotionCharUse", description = "APIs quản lý thuộc tính sử dụng của khuyến mãi giảm giá")
@RestController
@RequestMapping("/product-policy-service/v1/discount-promotion-char-use")
@RequiredArgsConstructor
public class DiscountPromotionCharUseController {

    private final DiscountPromotionCharUseService service;

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
            Long id) {
        RequestValidator.checkRange(id, "id", 0L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
        return StandardResponses.success(service.findById(id));
    }
}
