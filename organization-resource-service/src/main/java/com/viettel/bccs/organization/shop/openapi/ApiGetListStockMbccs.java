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
@Operation(operationId = "getListStockMbccs", summary = "Lấy danh sách kho số cho mBCCS theo user",
        description = "API cung cấp cho mBCCS lấy danh sách kho số của user. Trả về 3 loại kho được " +
                "đánh dấu bằng field type: 1 = kho đơn vị (mã shop của user), 2 = kho cá nhân (mã user), " +
                "3 = kho chức năng (resolve từ bảng STOCK_CHANNEL_MAPPING).")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = StockControllerExamples.STOCK_MBCCS_LIST_EXAMPLE))),
        @ApiResponse(responseCode = "404", description = "Mã user không tồn tại hoặc không ở trạng thái hoạt động")
})
public @interface ApiGetListStockMbccs {
}
