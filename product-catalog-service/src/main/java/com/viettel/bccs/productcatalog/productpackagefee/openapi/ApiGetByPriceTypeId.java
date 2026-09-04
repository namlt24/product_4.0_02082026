package com.viettel.bccs.productcatalog.productpackagefee.openapi;

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
@Operation(operationId = "getProductPackageFeeByPriceTypeId", summary = "Lấy danh sách phí theo ID loại giá",
        description = "Truy vấn danh sách PRODUCT_PACKAGE_FEE theo cột PRICE_TYPE_ID.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                            value = ProductPackageFeeControllerExamples.FEE_LIST_EXAMPLE)))})
public @interface ApiGetByPriceTypeId {
}
