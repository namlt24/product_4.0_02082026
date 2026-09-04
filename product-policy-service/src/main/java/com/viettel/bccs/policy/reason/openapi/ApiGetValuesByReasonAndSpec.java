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
@Operation(operationId = "getValuesByReasonAndSpec",
        summary = "Lấy danh sách giá trị thuộc tính của hình thức hòa mạng theo mã đặc tính",
        description = "Trả về danh sách giá trị (product_spec_char_value.value) đang gán cho reason (reasonId)"
                + " tại đặc tính có code = specCode, tra cứu qua REASON_CHAR_USE nối sang"
                + " ProductSpecChar/PRODUCT_SPEC_CHAR_VALUE (gọi cross-service product-catalog-service)."
                + " Chỉ tính các bản ghi REASON_CHAR_USE đang active. Trả về danh sách rỗng nếu reason"
                + " không có đặc tính khớp hoặc không có giá trị.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                                value = ReasonControllerExamples.VALUES_BY_REASON_AND_SPEC_EXAMPLE)))
})
public @interface ApiGetValuesByReasonAndSpec {
}
