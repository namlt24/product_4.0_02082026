package com.viettel.bccs.policy.discountpromotion.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionDTO;
import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionResponse;
import com.viettel.bccs.policy.discountpromotion.openapi.ApiFindById;
import com.viettel.bccs.policy.discountpromotion.openapi.ApiGetPromotionList;
import com.viettel.bccs.policy.discountpromotion.service.DiscountPromotionService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "DiscountPromotion", description = "APIs quản lý khuyến mãi giảm giá")
@RestController
@RequestMapping("/product-policy-service/v1/discountpromotion")
@RequiredArgsConstructor
public class DiscountPromotionController {

    private final DiscountPromotionService service;

    @ApiFindById
    @GetMapping("/findById/{id}")
    public StandardResponse<DiscountPromotionResponse> findById(
            @Parameter(description = "Id khuyến mãi giảm giá (DISCOUNT_PROMOTION_ID)", example = "1", required = true)
            @PathVariable
            Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @ApiGetPromotionList
    @GetMapping("/getPromotionList")
    public StandardResponse<List<DiscountPromotionDTO>> getPromotionList(
            @Parameter(description = "Id dịch vụ viễn thông", example = "100")
            @RequestParam(required = false)
            Long telecomServiceId,
            @Parameter(description = "Có kiểm tra trạng thái hay không", example = "false")
            @RequestParam(defaultValue = "false") boolean checkStatus,
            @Parameter(description = "Có kiểm tra ngày hiệu lực hay không", example = "false")
            @RequestParam(defaultValue = "false") boolean checkEffectDate,
            @Parameter(description = "Ngày hết hiệu lực để so sánh", example = "2026-08-11T02:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return StandardResponses.success(
                service.getPromotionList(telecomServiceId, checkStatus, checkEffectDate, endDate));
    }
}
