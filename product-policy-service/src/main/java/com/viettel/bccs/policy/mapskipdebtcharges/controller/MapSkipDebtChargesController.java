package com.viettel.bccs.policy.mapskipdebtcharges.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapskipdebtcharges.dto.response.MapSkipDebtChargesDTO;
import com.viettel.bccs.policy.mapskipdebtcharges.dto.response.MapSkipDebtChargesDTOFull;
import com.viettel.bccs.policy.mapskipdebtcharges.service.MapSkipDebtChargesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static com.viettel.bccs.policy.mapskipdebtcharges.openapi.MapSkipDebtChargesControllerExamples.*;

@RestController
@RequestMapping("/product-policy-service/v1/map-skip-debt-charges")
@RequiredArgsConstructor
@Validated
public class MapSkipDebtChargesController {

    private final MapSkipDebtChargesService service;

    @PostMapping("/getMapSkipDebtChargeFullInfo")
    @Operation(operationId = "getMapSkipDebtChargeFullInfo",
            summary = "Lấy thông tin đầy đủ MAP_SKIP_DEBT_CHARGES theo danh sách đầu vào",
            description = "API nhận danh sách MAP_SKIP_DEBT_CHARGES đầu vào, trả về danh sách gộp theo key",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MapSkipDebtChargesDTO.class)),
                            examples = @ExampleObject(name = "request", value = MAP_SKIP_DEBT_CHARGES_LIST_REQUEST_EXAMPLE))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = MAP_SKIP_DEBT_CHARGES_FULL_LIST_EXAMPLE)))
    })
    public StandardResponse<List<MapSkipDebtChargesDTOFull>> getMapSkipDebtChargeFullInfo(
            @Valid @RequestBody List<@Valid MapSkipDebtChargesDTO> mapSkipDebtChargesDTOs) throws Exception {
        return StandardResponses.success(service.getFullInfo(mapSkipDebtChargesDTOs));
    }
}
