package com.viettel.bccs.productcatalog.telecomservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.telecomservice.dto.response.TelecomServiceDTO;
import com.viettel.bccs.productcatalog.telecomservice.openapi.ApiGetTelServiceByAlias;
import com.viettel.bccs.productcatalog.telecomservice.service.TelecomServiceService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Telecom Service", description = "APIs quản lý dịch vụ viễn thông")
@RestController
@RequestMapping("/product-catalog-service/v1/telecom-service")
@RequiredArgsConstructor
public class TelecomServiceController {

    private final TelecomServiceService service;

    @GetMapping("/getTelServiceByAlias")
    @ApiGetTelServiceByAlias
    public StandardResponse<TelecomServiceDTO> getTelServiceByAlias(
            @Parameter(description = "Mã alias dịch vụ viễn thông", example = "MOB", required = true)
            @RequestParam(required = false)
            String alias) {
        return StandardResponses.success(service.getTelServiceByAlias(alias));
    }
}
