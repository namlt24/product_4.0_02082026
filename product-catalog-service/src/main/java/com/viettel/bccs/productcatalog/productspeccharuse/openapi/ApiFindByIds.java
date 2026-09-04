package com.viettel.bccs.productcatalog.productspeccharuse.openapi;

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
@Operation(operationId = "findProductSpecCharUseByIds",
    summary = "Lấy danh sách bản ghi sử dụng đặc tính sản phẩm theo danh sách ID",
    description = "Truy vấn nhiều bản ghi PRODUCT_SPEC_CHAR_USE theo danh sách"
            + " PROD_SPEC_CHAR_USE_ID truyền vào.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                            value = ProductSpecCharUseControllerExamples.SPEC_CHAR_USE_LIST_EXAMPLE)))})
public @interface ApiFindByIds {
}
