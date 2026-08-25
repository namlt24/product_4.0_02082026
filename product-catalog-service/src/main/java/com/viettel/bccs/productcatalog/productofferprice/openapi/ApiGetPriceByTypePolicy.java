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
        operationId = "getPriceByTypePolicy",
        summary = "Lấy danh sách giá bán theo loại giá và chính sách giá",
        description = "Truy vấn danh sách giá bán thiết bị (PRODUCT_OFFER_PRICE) đang active của 1 sản phẩm theo priceTypeId và pricePolicy, chỉ lấy bản ghi còn hiệu lực theo ngày hiện tại (EFFECT_DATETIME/EXPIRE_DATETIME). Kết quả sắp xếp tăng dần theo giá.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = ProductOfferPriceControllerExamples.PRICE_DTO_LIST_EXAMPLE)))
})
public @interface ApiGetPriceByTypePolicy {
}
