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
@Operation(operationId = "findAllMapBusinessSkipDebt", summary = "Lấy tất cả cấu hình bỏ qua công nợ",
        description = "Trả về toàn bộ bản ghi trong bảng MAP_BUSINESS_SKIP_DEBT, không phân trang, không lọc.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = MapBusinessSkipDebtControllerExamples.LIST_EXAMPLE)))
})
public @interface ApiFindAll {
}
