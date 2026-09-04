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
@Operation(operationId = "getPledgePriceInfoByOfferId",
        summary = "Lấy thông tin giá cam kết theo ID mặt hàng",
        description = "Truy vấn giá tiền, số tiền cam kết, số tháng cam kết và số tháng ứng trước " +
                "(PRICE_TYPE_ID=2) từ PRODUCT_OFFER_PRICE theo product_offering_id, " +
                "chỉ lấy bản ghi đang active và còn hiệu lực theo ngày hiện tại.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                            value = ProductOfferPriceControllerExamples.PLEDGE_PRICE_EXAMPLE)))})
public @interface ApiGetPledgePriceInfoByOfferId {
}
