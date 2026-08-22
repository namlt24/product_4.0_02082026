package com.viettel.bccs.productcatalog.telecomservice.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.telecomservice.dto.response.TelecomServiceDTO;
import com.viettel.bccs.productcatalog.telecomservice.service.TelecomServiceService;
import com.viettel.bccs.productcatalog.utils.RequestValidator;
import com.viettel.bccs.productcatalog.utils.ValidationPatterns;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.viettel.bccs.productcatalog.telecomservice.openapi.TelecomServiceControllerExamples.*;

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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = TELECOM_SERVICE_EXAMPLE)))
    })
    public StandardResponse<TelecomServiceDTO> getTelServiceByAlias(
            @Parameter(description = "Mã alias dịch vụ viễn thông", example = "MOB", required = true)
            @RequestParam(required = false)
            String alias) {
        RequestValidator.requireNotBlank(alias, "alias", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(alias, "alias", 3, "BCCS-CATALOG-VALIDATE-SIZE");
        RequestValidator.checkPattern(alias, "alias", ValidationPatterns.ALPHANUMERIC, "BCCS-CATALOG-VALIDATE-PATTERN");
        return StandardResponses.success(service.getTelServiceByAlias(alias));
    }
}
