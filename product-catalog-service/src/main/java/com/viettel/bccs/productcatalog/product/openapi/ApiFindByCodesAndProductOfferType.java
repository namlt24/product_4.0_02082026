package com.viettel.bccs.productcatalog.product.openapi;

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
@Operation(
        operationId = "API_DAUNOI_TT_PRODUCT_017",
        summary = "Tìm kiếm sản phẩm theo danh sách mã và loại sản phẩm"
)
@ApiResponses({
        @ApiResponse(responseCode = "200",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                            value = ProductOfferingControllerExamples.PRODUCT_LIST_EXAMPLE))),
                                       @ApiResponse(responseCode = "400")
})
public @interface ApiFindByCodesAndProductOfferType {
}
