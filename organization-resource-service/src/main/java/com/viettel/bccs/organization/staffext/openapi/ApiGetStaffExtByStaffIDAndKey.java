package com.viettel.bccs.organization.staffext.openapi;

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
@Operation(operationId = "getStaffExtByStaffIdAndKey", summary = "Lấy thông tin mở rộng theo staffId và key",
        description = "Tra cứu 1 bản ghi STAFF_EXT theo STAFF_ID và KEY.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(
                                name = "success",
                                value = StaffExtControllerExamples.STAFF_EXT_SINGLE_EXAMPLE))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy thông tin mở rộng với staffId/key tương ứng")
})
public @interface ApiGetStaffExtByStaffIDAndKey {
}
