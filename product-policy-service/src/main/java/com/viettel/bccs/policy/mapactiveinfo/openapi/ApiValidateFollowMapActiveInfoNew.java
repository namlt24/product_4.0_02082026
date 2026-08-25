package com.viettel.bccs.policy.mapactiveinfo.openapi;

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
@Operation(operationId = "validateFollowMapActiveInfoNew",
        summary = "Validate theo thông tin mapping mới",
        description = "Kiểm tra thông tin đấu nối theo bản ghi MAP_ACTIVE_INFO khớp nhất với các tiêu chí đầu vào " +
                "(staffCode, actionCode, offerIds, promotionCode, regReasonId, telServiceId, địa bàn, loại khách hàng...) " +
                "và trả về MapActiveInfoDTO tương ứng.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = MapActiveInfoValidateControllerExamples.VALIDATE_FOLLOW_MAP_ACTIVE_INFO_NEW_EXAMPLE)))
})
public @interface ApiValidateFollowMapActiveInfoNew {
}
