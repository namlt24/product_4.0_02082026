package com.viettel.bccs.policy.mapactiveinfo.openapi;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.GetProductCodeRequest;
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
@Operation(operationId = "getProductCode",
        summary = "API lấy danh sách gói cước theo map active info",
        description = "Trả về danh sách gói cước (product code) hợp lệ theo map active info. Luôn ép kiểu PRODUCT_CODE và checkProductStatus=true, không lọc theo VAS.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                content = @Content(schema = @Schema(implementation = GetProductCodeRequest.class),
                        examples = @ExampleObject(name = "request", value = MapActiveInfoProductControllerExamples.PRODUCT_CODE_REQUEST_EXAMPLE))))
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = MapActiveInfoProductControllerExamples.PRODUCT_CODE_LIST_EXAMPLE)))
})
public @interface ApiGetProductCode {
}
