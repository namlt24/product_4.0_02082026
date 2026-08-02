package com.viettel.bccs.policy.mapping.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapping.service.MappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Mapping", description = "APIs quản lý mapping dịch vụ bán hàng")
@RestController
@RequestMapping("/product-policy-service/v1/mapping")
@RequiredArgsConstructor
public class MappingController {

    private final MappingService service;

    @Operation(summary = "Tìm mã dịch vụ bán hàng theo lý do")
    @GetMapping("/findSaleServiceCodeByReason/{reasonId}")
    public StandardResponse<List<String>> findSaleServiceCodeByReason(
            @Parameter(description = "Id lý do") @PathVariable Long reasonId) {
        return StandardResponses.success(service.findSaleServiceCodeByReason(reasonId));
    }
}