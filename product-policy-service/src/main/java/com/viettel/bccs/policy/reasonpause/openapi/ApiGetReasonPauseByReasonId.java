package com.viettel.bccs.policy.reasonpause.openapi;

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
@Operation(operationId = "getReasonPauseByReasonId", summary = "Lấy danh sách kỳ tạm ngưng theo ID hình thức hòa mạng",
        description = "Tra cứu các bản ghi REASON_PAUSE theo REASON_ID.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = ReasonPauseControllerExamples.REASON_PAUSE_LIST_EXAMPLE)))
})
public @interface ApiGetReasonPauseByReasonId {
}
