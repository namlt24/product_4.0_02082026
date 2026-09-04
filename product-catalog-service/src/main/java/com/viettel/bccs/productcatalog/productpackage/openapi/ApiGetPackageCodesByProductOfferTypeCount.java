package com.viettel.bccs.productcatalog.productpackage.openapi;

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
@Operation(operationId = "getPackageCodesByProductOfferTypeCount",
    summary = "Lấy danh sách mã gói theo số lượng loại mặt hàng",
    description = "Truy vấn danh sách mã gói sản phẩm (product_package.code), loại trừ loại mặt hàng"
            + " excludeProdOfferType, tuỳ chọn lọc theo số lượng loại mặt hàng (packageNumber).")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                            value = ProductPackageControllerExamples.PACKAGE_CODES_EXAMPLE)))})
public @interface ApiGetPackageCodesByProductOfferTypeCount {
}
