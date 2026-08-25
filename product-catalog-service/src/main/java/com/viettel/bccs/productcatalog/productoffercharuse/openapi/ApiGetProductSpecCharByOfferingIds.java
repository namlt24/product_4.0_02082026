package com.viettel.bccs.productcatalog.productoffercharuse.openapi;

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
@Operation(operationId = "getProductSpecCharByOfferingIds",
        summary = "Lấy đặc tính sản phẩm theo danh sách offering IDs",
        description = "Batch fetch đặc tính sản phẩm cho nhiều offering IDs trong 1 query, trả về Map<offeringId, list specChar>")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = ProductOfferCharUseControllerExamples.SPEC_CHAR_MAP_EXAMPLE)))
})
public @interface ApiGetProductSpecCharByOfferingIds {
}
