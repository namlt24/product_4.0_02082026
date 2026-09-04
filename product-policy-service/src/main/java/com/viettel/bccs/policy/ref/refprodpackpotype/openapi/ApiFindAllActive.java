package com.viettel.bccs.policy.ref.refprodpackpotype.openapi;

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
@Operation(operationId = "API_POLICY_REF_001",
        summary = "Lấy danh sách REF_PROD_PACK_PO_TYPE đang hiệu lực",
        description = "API lấy tất cả REF_PROD_PACK_PO_TYPE có trạng thái hiệu lực")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                                value = RefProdPackPoTypeControllerExamples.REF_PROD_PACK_PO_TYPE_LIST_EXAMPLE)))
})
public @interface ApiFindAllActive {
}
