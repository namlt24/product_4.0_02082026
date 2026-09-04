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
@Operation(operationId = "getReasonCharacter", summary = "Lấy danh sách mã thuộc tính của hình thức hòa mạng",
        description = "Trả về danh sách mã thuộc tính (product_spec_char.code) đang gán cho reason (reasonId),"
                + " tra cứu qua REASON_CHAR_USE. Chỉ tính các bản ghi REASON_CHAR_USE đang active."
                + " Trả về danh sách rỗng nếu reason không có thuộc tính nào.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                                value = ReasonControllerExamples.REASON_CHARACTER_LIST_EXAMPLE)))
})
public @interface ApiGetReasonCharacter {
}
