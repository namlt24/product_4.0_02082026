package com.viettel.bccs.organization.staff.openapi;

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
@Operation(operationId = "findActiveStaffByStaffCodeWithChannelOfSalePoint",
        summary = "Lấy nhân viên active theo mã kèm cờ isChannelOfSalePoint",
        description = "Tra cứu 1 bản ghi STAFF active theo STAFF_CODE và tính thêm trường isChannelOfSalePoint"
                + " từ loại kênh của nhân viên.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(
                                name = "success",
                                value = StaffControllerExamples.STAFF_DTO_WITH_CHANNEL_OF_SALE_POINT_EXAMPLE))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên với mã tương ứng")
})
public @interface ApiFindActiveByStaffCodeWithChannelOfSalePoint {
}
