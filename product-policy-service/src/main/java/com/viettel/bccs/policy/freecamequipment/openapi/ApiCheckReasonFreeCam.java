package com.viettel.bccs.policy.freecamequipment.openapi;

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
@Operation(operationId = "checkReasonFreeCam", summary = "Kiểm tra danh sách thiết bị CAM miễn phí theo gói sản phẩm (sale service), phục vụ getPriceInServices",
        description = "Trả về danh sách bản ghi FREE_CAM_EQUIPMENT khớp với gói sản phẩm (product package / sale service) truyền vào.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = FreeCamEquipmentControllerExamples.CHECK_REASON_FREE_CAM_EXAMPLE)))
})
public @interface ApiCheckReasonFreeCam {
}
