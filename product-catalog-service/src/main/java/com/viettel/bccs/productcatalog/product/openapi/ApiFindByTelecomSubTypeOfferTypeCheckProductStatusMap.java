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
        operationId = "findByTelecomSubTypeOfferTypeCheckProductStatusMap",
        summary = "Tìm kiếm sản phẩm theo dịch vụ viễn thông, loại thuê bao và loại sản phẩm,"
                + " trả về map productOfferingId -> subType")
@ApiResponses({
        @ApiResponse(responseCode = "200",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                            value = ProductOfferingControllerExamples.PRODUCT_SUB_TYPE_MAP_LIST_EXAMPLE)))})
public @interface ApiFindByTelecomSubTypeOfferTypeCheckProductStatusMap {
}
