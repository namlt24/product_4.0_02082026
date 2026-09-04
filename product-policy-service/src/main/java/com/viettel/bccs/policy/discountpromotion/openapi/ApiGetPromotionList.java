package com.viettel.bccs.policy.discountpromotion.openapi;

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
@Operation(operationId = "getPromotionList", summary = "Lấy danh sách khuyến mãi giảm giá",
        description = "Tra cứu danh sách DISCOUNT_PROMOTION theo dịch vụ viễn thông,"
                + " có thể lọc theo trạng thái và ngày hiệu lực.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                                value = DiscountPromotionControllerExamples.PROMOTION_LIST_EXAMPLE)))
})
public @interface ApiGetPromotionList {
}
