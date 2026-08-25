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
@Operation(operationId = "getByCustTypeAndChannelType", summary = "Lấy mapping theo loại khách hàng và loại kênh",
        description = "Tra cứu 1 bản ghi CUST_CHANNEL_TYPE_MAPPING theo cặp CUST_TYPE và CHANNEL_TYPE_ID.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = CustChannelTypeMappingControllerExamples.MAPPING_SINGLE_EXAMPLE))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy mapping tương ứng")
})
public @interface ApiGetByCustTypeAndChannelType {
}
