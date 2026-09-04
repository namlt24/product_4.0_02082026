package com.viettel.bccs.productcatalog.optionset.openapi;

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
@Operation(operationId = "getValueByTwoCodeOption", summary = "Lấy giá trị option set theo mã option set và tên",
        description = "Tra cứu VALUE của 1 bản ghi OPTION_SET_VALUE theo mã nhóm option set"
                + " (optSetCode) và tên (name).")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                            value = OptionSetValueControllerExamples.STRING_VALUE_EXAMPLE)))})
public @interface ApiGetValueByTwoCodeOption {
}
