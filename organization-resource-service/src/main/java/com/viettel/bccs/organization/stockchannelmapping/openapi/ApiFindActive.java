package com.viettel.bccs.organization.stockchannelmapping.openapi;

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
@Operation(operationId = "findActiveStockChannelMapping", summary = "Lấy toàn bộ mapping đang hiệu lực",
        description = "Trả về danh sách toàn bộ bản ghi STOCK_CHANNEL_MAPPING đang hiệu lực (status = 1).")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(
                                name = "success",
                                value = StockChannelMappingControllerExamples.MAPPING_LIST_EXAMPLE))),
})
public @interface ApiFindActive {
}
