package com.viettel.bccs.policy.discountpromotioncharuse.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.discountpromotioncharuse.dto.response.DiscountPromotionCharUseResponse;
import com.viettel.bccs.policy.discountpromotioncharuse.openapi.ApiFindById;
import com.viettel.bccs.policy.discountpromotioncharuse.service.DiscountPromotionCharUseService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "DiscountPromotionCharUse", description = "APIs quản lý thuộc tính sử dụng của khuyến mãi giảm giá")
@RestController
@RequestMapping("/product-policy-service/v1/discount-promotion-char-use")
@RequiredArgsConstructor
public class DiscountPromotionCharUseController {

    private final DiscountPromotionCharUseService service;

    @ApiFindById
    @GetMapping("/findById/{id}")
    public StandardResponse<DiscountPromotionCharUseResponse> findById(
            @Parameter(description = "Id thuộc tính sử dụng khuyến mãi (DISCOUNT_PROMOTION_CHAR_USE_ID)", example = "1", required = true)
            @PathVariable
            Long id) {
        return StandardResponses.success(service.findById(id));
    }
}
