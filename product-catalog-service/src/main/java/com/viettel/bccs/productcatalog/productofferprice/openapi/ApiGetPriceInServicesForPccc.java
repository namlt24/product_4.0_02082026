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
        operationId = "getPriceInServicesForPccc",
        summary = "Lấy danh sách giá bán thiết bị phục vụ quản lý cước PCCC",
        description = "Truy vấn danh sách giá bán thiết bị theo các tiêu chí: ID/Code gói sản phẩm,"
                + " loại sản phẩm, ID sản phẩm và chính sách giá. Phục vụ cho nghiệp vụ quản lý cước PCCC.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                                value = ProductOfferPriceControllerExamples.PRICE_DTO_LIST_EXAMPLE)))
})
public @interface ApiGetPriceInServicesForPccc {
}
