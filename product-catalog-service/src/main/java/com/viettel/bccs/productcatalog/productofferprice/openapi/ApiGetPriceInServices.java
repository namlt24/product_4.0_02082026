package com.viettel.bccs.productcatalog.productofferprice.openapi;

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
        operationId = "getPriceInServices",
        summary = "Lấy danh sách giá bán thiết bị trong gói dịch vụ",
        description = "Truy vấn danh sách giá bán thiết bị (ProductOfferPrice) theo các tiêu chí:"
                + " ID/Code gói sản phẩm, loại sản"
                + " phẩm, ID sản phẩm và chính sách giá. " +
                "Hỗ trợ tính giá thiết bị CAM theo loại (indoor/outdoor) và giá thiết bị thông thường. " +
                "Kết quả bao gồm thông tin giá, tên sản phẩm, và giá thiết bị khuyến mãi.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                            value = ProductOfferPriceControllerExamples.PRICE_RESPONSE_LIST_EXAMPLE)))})
public @interface ApiGetPriceInServices {
}
