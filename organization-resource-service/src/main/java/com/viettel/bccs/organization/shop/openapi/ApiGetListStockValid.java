package com.viettel.bccs.organization.shop.openapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.http.MediaType;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.organization.shop.dto.request.GetListStockValidRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Operation(operationId = "getListStockValid", summary = "Lọc danh sách kho số hợp lệ theo user từ danh sách shopId",
        requestBody = @RequestBody(required = true,
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = GetListStockValidRequest.class))))
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(
                                name = "success",
                                value = StockControllerExamples.STOCK_VALID_LIST_EXAMPLE))),
        @ApiResponse(responseCode = "404", description = "Mã user không tồn tại hoặc không ở trạng thái hoạt động")
})
public @interface ApiGetListStockValid {
}
