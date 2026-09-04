package com.viettel.bccs.policy.mapactiveinfo.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.GetProductCodeByMapActiveInfoRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(operationId = "getProductCodeByMapActiveInfo",
        summary = "API lấy danh sách gói cước",
        description = "API lấy danh sách gói cước theo map active info,"
                + " loc theo vai tro nhan vien (M2M, goi dac biet, goi thuong)",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                content = @Content(schema = @Schema(implementation = GetProductCodeByMapActiveInfoRequest.class),
                        examples = @ExampleObject(name = "request",
                                value = MapActiveInfoProductControllerExamples
                                .PRODUCT_CODE_BY_MAP_ACTIVE_INFO_REQUEST_EXAMPLE))))
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                                value = MapActiveInfoProductControllerExamples.PRODUCT_CODE_LIST_EXAMPLE)))
})
public @interface ApiGetProductCodeByMapActiveInfo {
}
