package com.viettel.bccs.policy.reason.openapi;

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
@Operation(operationId = "getReasonIdByTypeAndCode",
        summary = "Tìm reasonId theo mã lý do, mã hành động và dịch vụ viễn thông",
        description = "Tra cứu REASON còn hiệu lực, khớp reason_code (regType), reason_type của actionCode"
                + " và telecomServiceId (hoặc reason không ràng buộc dịch vụ). Trả về reasonId bản ghi đầu tiên"
                + " (sắp theo name ASC), hoặc null nếu không tìm thấy. Phục vụ product-catalog-service"
                + " (API getListStockTypeWS).")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                                value = ReasonControllerExamples.REASON_ID_EXAMPLE)))
})
public @interface ApiGetReasonIdByTypeAndCode {
}
