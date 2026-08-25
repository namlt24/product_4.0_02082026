package com.viettel.bccs.policy.reason.openapi;

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
@Operation(operationId = "checkAttReason", summary = "Kiểm tra hình thức hòa mạng (reason) có đặc tính theo mã",
        description = "Kiểm tra reason (reasonId) có gán đặc tính (product_spec_char, tra cứu qua REASON_CHAR_USE) theo attributeCode hay không. Chỉ tính các bản ghi REASON_CHAR_USE đang active.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = ReasonControllerExamples.CHECK_ATT_REASON_EXAMPLE)))
})
public @interface ApiCheckAttReason {
}
