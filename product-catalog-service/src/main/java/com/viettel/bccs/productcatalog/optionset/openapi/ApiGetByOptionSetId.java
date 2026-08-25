package com.viettel.bccs.productcatalog.optionset.openapi;

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
@Operation(operationId = "getOptionSetValueByOptionSetId", summary = "Lấy danh sách giá trị theo id nhóm option set",
        description = "Tra cứu các bản ghi OPTION_SET_VALUE có OPTION_SET_ID khớp tham số.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = OptionSetValueControllerExamples.OPTION_SET_VALUE_LIST_EXAMPLE)))
})
public @interface ApiGetByOptionSetId {
}
