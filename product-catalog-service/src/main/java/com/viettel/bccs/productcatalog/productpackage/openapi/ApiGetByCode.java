package com.viettel.bccs.productcatalog.productpackage.openapi;

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
@Operation(
        operationId = "getProductPackageByCode",
        summary = "Lấy thông tin gói sản phẩm theo mã gói",
        description = "Truy vấn thông tin gói sản phẩm (product package) theo mã gói. Trả về danh sách vì một mã gói có thể map với nhiều bản ghi (theo telecomServiceId khác nhau). Chỉ trả về bản ghi có `status = 1`."
)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = ProductPackageControllerExamples.PRODUCT_PACKAGE_LIST_EXAMPLE)))
})
public @interface ApiGetByCode {
}
