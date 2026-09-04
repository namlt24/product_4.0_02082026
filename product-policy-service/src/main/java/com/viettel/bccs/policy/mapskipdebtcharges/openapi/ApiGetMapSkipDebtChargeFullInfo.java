package com.viettel.bccs.policy.mapskipdebtcharges.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.policy.mapskipdebtcharges.dto.response.MapSkipDebtChargesDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(operationId = "getMapSkipDebtChargeFullInfo",
        summary = "Lấy thông tin đầy đủ MAP_SKIP_DEBT_CHARGES theo danh sách đầu vào",
        description = "API nhận danh sách MAP_SKIP_DEBT_CHARGES đầu vào, trả về danh sách gộp theo key",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                content = @Content(array = @ArraySchema(schema = @Schema(implementation = MapSkipDebtChargesDTO.class)),
                        examples = @ExampleObject(name = "request",
                                value = MapSkipDebtChargesControllerExamples
                                .MAP_SKIP_DEBT_CHARGES_LIST_REQUEST_EXAMPLE))))
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                                value = MapSkipDebtChargesControllerExamples.MAP_SKIP_DEBT_CHARGES_FULL_LIST_EXAMPLE)))
})
public @interface ApiGetMapSkipDebtChargeFullInfo {
}
