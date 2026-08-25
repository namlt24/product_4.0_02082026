package com.viettel.bccs.productcatalog.productspeccharvalue.openapi;

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
@Operation(operationId = "findProductSpecCharValueByIds", summary = "Lấy danh sách giá trị thuộc tính sản phẩm theo danh sách ID",
        description = "Truy vấn nhiều bản ghi PRODUCT_SPEC_CHAR_VALUE theo danh sách PRODUCT_SPEC_CHAR_VALUE_ID truyền vào.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = ProductSpecCharValueControllerExamples.SPEC_CHAR_VALUE_LIST_EXAMPLE)))
})
public @interface ApiFindByIds {
}
