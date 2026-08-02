package com.viettel.bccs.policy.discountpromotioncharuse.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.discountpromotioncharuse.dto.response.DiscountPromotionCharUseResponse;
import com.viettel.bccs.policy.discountpromotioncharuse.service.DiscountPromotionCharUseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product-policy-service/v1/discount-promotion-char-use")
@RequiredArgsConstructor
public class DiscountPromotionCharUseController {

    private final DiscountPromotionCharUseService service;

    @GetMapping("/findById/{id}")
    public StandardResponse<DiscountPromotionCharUseResponse> findById(@PathVariable Long id) {
        return StandardResponses.success(service.findById(id));
    }
}