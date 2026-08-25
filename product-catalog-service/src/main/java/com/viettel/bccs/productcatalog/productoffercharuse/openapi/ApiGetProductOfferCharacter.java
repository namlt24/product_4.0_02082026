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
@Operation(operationId = "getProductOfferCharacter",
        summary = "Lấy danh sách đặc tính của một sản phẩm",
        description = "Truy vấn toàn bộ đặc tính (product_spec_char) và giá trị (product_spec_char_value) của một sản phẩm (product_offering) qua bảng product_offer_char_use. Trả về danh sách phần tử gồm productCode và đặc tính + giá trị tương ứng.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = ProductOfferCharUseControllerExamples.OFFER_CHARACTER_EXAMPLE)))
})
public @interface ApiGetProductOfferCharacter {
}
