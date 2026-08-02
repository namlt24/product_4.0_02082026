package com.viettel.bccs.policy.discountpromotion.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionResponse;
import com.viettel.bccs.policy.discountpromotion.service.DiscountPromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/product-policy-service/v1/discountpromotion")
@RequiredArgsConstructor
public class DiscountPromotionController {

    private final DiscountPromotionService service;

    @GetMapping("/findById/{id}")
    public StandardResponse<DiscountPromotionResponse> findById(@PathVariable Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @GetMapping("/getPromotionList")
    public StandardResponse<List<DiscountPromotionResponse>> getPromotionList(
            @RequestParam(required = false) Long telecomServiceId,
            @RequestParam(defaultValue = "false") boolean checkStatus,
            @RequestParam(defaultValue = "false") boolean checkEffectDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return StandardResponses.success(
                service.getPromotionList(telecomServiceId, checkStatus, checkEffectDate, endDate));
    }
}