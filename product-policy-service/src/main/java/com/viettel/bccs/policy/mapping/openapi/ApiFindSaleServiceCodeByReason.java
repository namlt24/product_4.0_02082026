package com.viettel.bccs.policy.mapping.openapi;

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
@Operation(operationId = "findSaleServiceCodeByReason", summary = "Tìm mã dịch vụ bán hàng theo lý do",
        description = "Trả về danh sách SALE_SERVICE_CODE trong bảng MAPPING khớp với REASON_ID truyền vào.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = MappingControllerExamples.SALE_SERVICE_CODE_LIST_EXAMPLE)))
})
public @interface ApiFindSaleServiceCodeByReason {
}
