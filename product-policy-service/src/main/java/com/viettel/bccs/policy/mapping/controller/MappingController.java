package com.viettel.bccs.policy.mapping.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapping.openapi.ApiFindSaleServiceCodeByReason;
import com.viettel.bccs.policy.mapping.openapi.ApiGetMappingReasonProductOfferPrice;
import com.viettel.bccs.policy.mapping.openapi.ApiGetSaleServiceCode;
import com.viettel.bccs.policy.mapping.service.MappingService;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Mapping", description = "APIs quản lý mapping dịch vụ bán hàng")
@RestController
@RequestMapping("/product-policy-service/v1/mapping")
@RequiredArgsConstructor
public class MappingController {

    private final MappingService service;

    @ApiFindSaleServiceCodeByReason
    @GetMapping("/findSaleServiceCodeByReason/{reasonId}")
    public StandardResponse<List<String>> findSaleServiceCodeByReason(
            @Parameter(description = "Id lý do", example = "1", required = true)
            @PathVariable
            Long reasonId) {
        return StandardResponses.success(service.findSaleServiceCodeByReason(reasonId));
    }

    @ApiGetMappingReasonProductOfferPrice
    @GetMapping("/getMappingReasonProductOfferPrice/{productPackageId}")
    public StandardResponse<List<ReasonResponse>> getMappingReasonProductOfferPrice(
            @Parameter(description = "Id gói sản phẩm (product package / sale service)", example = "1", required = true)
            @PathVariable
            Long productPackageId) {
        return StandardResponses.success(service.getMappingReasonProductOfferPrice(productPackageId));
    }

    @ApiGetSaleServiceCode
    @GetMapping("/getSaleServiceCode")
    public StandardResponse<String> getSaleServiceCode(
            @Parameter(description = "ID dịch vụ viễn thông", example = "1")
            @RequestParam(required = false)
            Long telecomServiceId,
            @Parameter(description = "Id lý do", example = "1", required = true)
            @RequestParam(required = false)
            Long reasonId,
            @Parameter(description = "Mã gói cước", example = "POBAS")
            @RequestParam(required = false)
            String productCode,
            @Parameter(description = "Mã hành động", example = "00")
            @RequestParam(required = false)
            String actionCode) {
        return StandardResponses.success(service.getSaleServiceCode(telecomServiceId, reasonId, productCode,
                actionCode));
    }
}
