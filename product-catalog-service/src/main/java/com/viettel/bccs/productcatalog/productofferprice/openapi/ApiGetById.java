package com.viettel.bccs.productcatalog.productofferprice.openapi;

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
        operationId = "getProductOfferPriceById",
        summary = "Lấy thông tin giá bán thiết bị theo ID",
        description = "Truy vấn chi tiết giá bán thiết bị (PRODUCT_OFFER_PRICE) theo productOfferPriceId. Trả về đầy đủ thông tin bao gồm giá, VAT, thời gian hiệu lực, chính sách giá và các trường audit.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = ProductOfferPriceControllerExamples.PRICE_DTO_EXAMPLE)))
})
public @interface ApiGetById {
}
