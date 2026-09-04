package com.viettel.bccs.organization.shop.openapi;

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
@Operation(operationId = "findActiveByShopIds",
        summary = "Tìm danh sách cửa hàng active theo nhiều shopId",
        description = "Truy vấn danh sách cửa hàng có status = 1 theo danh sách shopId. " +
                "Query được chia batch (100 bản ghi/batch) để tránh lỗi ORA-01795 khi danh sách lớn.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = ShopControllerExamples.SHOP_LIST_EXAMPLE)))
})
public @interface ApiFindActiveByShopIds {
}
