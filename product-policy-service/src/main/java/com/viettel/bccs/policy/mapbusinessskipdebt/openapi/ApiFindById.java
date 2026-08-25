package com.viettel.bccs.policy.mapbusinessskipdebt.openapi;

import com.viettel.bccs.common.api.response.StandardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(operationId = "findMapBusinessSkipDebtById", summary = "Lấy cấu hình bỏ qua công nợ theo ID",
        description = "Tra cứu 1 bản ghi MAP_BUSINESS_SKIP_DEBT theo MAP_ID (khoá chính).")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = MapBusinessSkipDebtControllerExamples.SINGLE_EXAMPLE)))
})
public @interface ApiFindById {
}
