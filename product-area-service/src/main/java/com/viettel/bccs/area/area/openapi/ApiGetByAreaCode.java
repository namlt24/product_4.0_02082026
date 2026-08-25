package com.viettel.bccs.area.area.openapi;

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
@Operation(operationId = "getAreaByCode", summary = "Lấy địa bàn theo mã",
        description = "Tra cứu 1 bản ghi AREA theo AREA_CODE (khoá chính). Ví dụ dùng dữ liệu mẫu 'A076' (An Giang) đã có sẵn trong DB.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = AreaControllerExamples.AREA_SINGLE_EXAMPLE))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy địa bàn với mã tương ứng")
})
public @interface ApiGetByAreaCode {
}
