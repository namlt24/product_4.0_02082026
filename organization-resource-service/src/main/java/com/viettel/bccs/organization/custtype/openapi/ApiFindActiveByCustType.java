package com.viettel.bccs.organization.custtype.openapi;

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
@Operation(operationId = "findActiveByCustType", summary = "Lấy loại khách hàng đang hiệu lực theo mã",
        description = "Tra cứu 1 bản ghi CUST_TYPE đang hiệu lực (status = 1) theo CUST_TYPE (khoá chính).")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = CustTypeControllerExamples.CUST_TYPE_SINGLE_EXAMPLE))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy loại khách hàng với mã tương ứng")
})
public @interface ApiFindActiveByCustType {
}
