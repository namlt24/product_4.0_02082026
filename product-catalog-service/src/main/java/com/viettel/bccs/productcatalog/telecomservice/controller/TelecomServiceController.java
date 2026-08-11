package com.viettel.bccs.productcatalog.telecomservice.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.telecomservice.dto.response.TelecomServiceDTO;
import com.viettel.bccs.productcatalog.telecomservice.service.TelecomServiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Telecom Service", description = "APIs quản lý dịch vụ viễn thông")
@RestController
@RequestMapping("/product-catalog-service/v1/telecom-service")
@RequiredArgsConstructor
@Validated
public class TelecomServiceController {

    private final TelecomServiceService service;

    private static final String TELECOM_SERVICE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "telecomServiceId": 1,
                "name": "Di động",
                "status": "1",
                "description": "Dịch vụ di động",
                "serviceAlias": "MOB",
                "createUser": "system",
                "createDatetime": "2026-01-01T00:00:00",
                "updateUser": null,
                "updateDatetime": null
              }
            }""";

    @GetMapping("/getTelServiceByAlias")
    @Operation(
            operationId = "getTelServiceByAlias",
            summary = "Tìm dịch vụ viễn thông theo alias",
            description = "Tìm dịch vụ viễn thông đang active (status = 1) theo mã alias (service_alias)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = TELECOM_SERVICE_EXAMPLE)))
    })
    public StandardResponse<TelecomServiceDTO> getTelServiceByAlias(
            @Parameter(description = "Mã alias dịch vụ viễn thông", example = "MOB", required = true)
            @RequestParam
            @Size(min = 1, max = 3, message = "alias tối đa 3 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9]{1,3}$", message = "alias chỉ gồm chữ và số")
            String alias) {
        return StandardResponses.success(service.getTelServiceByAlias(alias));
    }
}
