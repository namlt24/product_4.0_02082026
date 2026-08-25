package com.viettel.bccs.productcatalog.product.openapi;

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
@Operation(operationId = "getSubTypeByProductCode",
        summary = "Lấy loại thuê bao theo mã mặt hàng cho mBCCS")
@ApiResponses({
        @ApiResponse(responseCode = "200",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = ProductOfferingControllerExamples.SUB_TYPE_BY_PRODUCT_CODE_EXAMPLE))),
        @ApiResponse(responseCode = "400")
})
public @interface ApiGetSubTypeByProductCode {
}
