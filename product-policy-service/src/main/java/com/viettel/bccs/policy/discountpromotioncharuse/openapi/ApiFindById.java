package com.viettel.bccs.policy.discountpromotioncharuse.openapi;

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
@Operation(operationId = "findDiscountPromotionCharUseById", summary = "Lấy thuộc tính sử dụng khuyến mãi theo id",
        description = "Tra cứu 1 bản ghi DISCOUNT_PROMOTION_CHAR_USE theo DISCOUNT_PROMOTION_CHAR_USE_ID (khoá chính).")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = DiscountPromotionCharUseControllerExamples.FIND_BY_ID_EXAMPLE)))
})
public @interface ApiFindById {
}
