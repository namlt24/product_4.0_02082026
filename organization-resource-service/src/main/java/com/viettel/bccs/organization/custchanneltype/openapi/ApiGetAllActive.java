package com.viettel.bccs.organization.custchanneltype.openapi;

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
@Operation(operationId = "getAllActiveCustChannelTypeMapping", summary = "Lấy toàn bộ mapping đang hiệu lực",
        description = "Trả về danh sách toàn bộ bản ghi CUST_CHANNEL_TYPE_MAPPING đang hiệu lực (status = 1).")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = CustChannelTypeMappingControllerExamples.MAPPING_LIST_EXAMPLE)))
})
public @interface ApiGetAllActive {
}
