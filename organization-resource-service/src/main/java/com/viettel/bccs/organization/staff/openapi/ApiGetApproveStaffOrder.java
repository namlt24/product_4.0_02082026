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
@Operation(operationId = "getApproveStaffOrder",
        summary = "API xác định nhân viên duyệt đơn theo mã nhân viên",
        description = "API xác định nhân viên duyệt đơn cho một staffCode. Ưu tiên lần lượt:"
                + " staff_owner_id của nhân viên (B3), " +
                "staff_owner_id của shop (B4), rồi giật lên shop cấp 3 theo shop_path"
                        + " và chọn ngẫu nhiên 1 nhân viên trong shop đó (B5). " +
                "Trả về DTO rút gọn (staffCode, name, staffId); trả null nếu không tìm được người duyệt.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(
                                name = "success",
                                value = StaffControllerExamples.APPROVE_STAFF_ORDER_EXAMPLE))),
})
public @interface ApiGetApproveStaffOrder {
}
