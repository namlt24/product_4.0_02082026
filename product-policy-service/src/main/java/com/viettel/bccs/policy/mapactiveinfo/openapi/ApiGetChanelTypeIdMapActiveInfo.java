package com.viettel.bccs.policy.mapactiveinfo.openapi;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.ChanelTypeIdRequest;
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
@Operation(operationId = "getChanelTypeIdMapActiveInfo",
        summary = "Lấy channelTypeId dùng cho map active info",
        description = "Thông tin nhân viên/shop (channelTypeId, shopChanelTypeId, pointOfSale)",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                content = @Content(schema = @Schema(implementation = ChanelTypeIdRequest.class),
                        examples = @ExampleObject(name = "request", value = MapActiveInfoQuerryControllerExamples.CHANEL_TYPE_ID_REQUEST_EXAMPLE))))
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = MapActiveInfoQuerryControllerExamples.CHANEL_TYPE_ID_EXAMPLE))),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên với staffId tương ứng")
})
public @interface ApiGetChanelTypeIdMapActiveInfo {
}
