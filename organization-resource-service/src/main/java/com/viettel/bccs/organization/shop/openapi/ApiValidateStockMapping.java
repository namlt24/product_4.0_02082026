package com.viettel.bccs.organization.shop.openapi;

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
@Operation(operationId = "validateStockMapping", summary = "Validate kho số có được mapping với user không",
        description = "API cung cấp cho IM kiểm tra kho số có được mapping với user hay không. " +
                "Kiểm tra theo thứ tự: kho cá nhân (staffCode), kho đơn vị (shopCode), " +
                "kho chức năng (giật cấp: user → cửa hàng → loại kênh).")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Kho số hợp lệ và được mapping với user",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = StockControllerExamples.VALIDATE_STOCK_MAPPING_EXAMPLE))),
        @ApiResponse(responseCode = "400", description = "User không tồn tại, kho không tồn tại, hoặc kho không được mapping với user")
})
public @interface ApiValidateStockMapping {
}
