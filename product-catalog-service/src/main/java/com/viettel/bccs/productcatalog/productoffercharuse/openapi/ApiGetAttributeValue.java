package com.viettel.bccs.productcatalog.productoffercharuse.openapi;

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
@Operation(operationId = "API_DAUNOI_TT_PRODUCT_020",
        summary = "Lấy giá trị thuộc tính sản phẩm",
        description = "Truy vấn giá trị của một đặc tính (attribute) theo offerId (product_offering.id)"
                + " và attributeName"
                + " (product_spec_char.code). Ưu tiên trả về SPECIFIC_VALUE, fallback về VALUE.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                            value = ProductOfferCharUseControllerExamples.ATTRIBUTE_VALUE_EXAMPLE)))})
public @interface ApiGetAttributeValue {
}
