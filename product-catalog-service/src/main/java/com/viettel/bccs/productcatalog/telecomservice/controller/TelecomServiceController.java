package com.viettel.bccs.productcatalog.telecomservice.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.telecomservice.dto.response.TelecomServiceDTO;
import com.viettel.bccs.productcatalog.telecomservice.service.TelecomServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Telecom Service", description = "APIs quản lý dịch vụ viễn thông")
@RestController
@RequestMapping("/product-catalog-service/v1/telecom-service")
@RequiredArgsConstructor
public class TelecomServiceController {

    private final TelecomServiceService service;

    @GetMapping("/getTelServiceByAlias")
    @Operation(
            operationId = "getTelServiceByAlias",
            summary = "Tìm dịch vụ viễn thông theo alias",
            description = "Tìm dịch vụ viễn thông đang active (status = 1) theo mã alias (service_alias)."
    )
    public StandardResponse<TelecomServiceDTO> getTelServiceByAlias(
            @Parameter(description = "Mã alias dịch vụ viễn thông", example = "MOB")
            @RequestParam String alias) {
        return StandardResponses.success(service.getTelServiceByAlias(alias));
    }
}
