package com.viettel.bccs.organization.identitytype.openapi;

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
@Operation(operationId = "findByIdType",
        summary = "API lấy thông tin loại giấy tờ theo mã",
        description = "Trả về thông tin chi tiết 1 loại giấy tờ (IDENTITY_TYPE) đang hiệu lực (status = 1)"
                + " theo mã loại giấy tờ (idType). Trả lỗi nếu không tìm thấy hoặc loại giấy tờ đã inactive.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(
                                name = "success",
                                value = IdentityTypeControllerExamples.IDENTITY_TYPE_SINGLE_EXAMPLE))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy loại giấy tờ với mã tương ứng")
})
public @interface ApiFindByIdType {
}
