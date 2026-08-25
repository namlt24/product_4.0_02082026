package com.viettel.bccs.policy.mapping.openapi;

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
@Operation(operationId = "getMappingReasonProductOfferPrice", summary = "Lấy danh sách lý do (reason) mapping theo gói sản phẩm (sale service), phục vụ getPriceInServices",
        description = "Trả về danh sách REASON khớp với gói sản phẩm (product package / sale service) truyền vào.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = MappingControllerExamples.REASON_LIST_EXAMPLE)))
})
public @interface ApiGetMappingReasonProductOfferPrice {
}
