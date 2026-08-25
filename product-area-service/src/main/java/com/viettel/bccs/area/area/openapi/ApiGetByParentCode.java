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
@Operation(operationId = "getAreaByParentCode", summary = "Lấy danh sách địa bàn con theo mã cha",
        description = "Tra cứu các bản ghi AREA có PARENT_CODE khớp tham số. Ví dụ dùng 'A076' (An Giang) trả về các quận/huyện trực thuộc.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = AreaControllerExamples.AREA_LIST_EXAMPLE)))
})
public @interface ApiGetByParentCode {
}
