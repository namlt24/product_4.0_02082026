package com.viettel.bccs.policy.reason.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.viettel.bccs.common.api.response.StandardResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(operationId = "getListReasonByActionCodeAndTelServiceForAudit",
        summary = "Lấy danh sách hình thức hòa mạng theo mã hành động, dịch vụ viễn thông",
        description = "Tra cứu danh sách REASON phục vụ audit theo actionCode, telServiceId, payType.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                                value = ReasonControllerExamples.REASON_DTO_LIST_EXAMPLE)))
})
public @interface ApiGetListReasonByActionCodeAndTelServiceForAudit {
}
