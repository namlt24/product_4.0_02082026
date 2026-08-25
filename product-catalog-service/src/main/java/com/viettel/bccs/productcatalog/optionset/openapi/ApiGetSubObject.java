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
@Operation(operationId = "API_PRODUCT_014",
        summary = "API lấy danh sách đối tượng con",
        description = "API lấy danh sách đối tượng con (sub-object) phục vụ MDealer, dựa trên loại khách hàng và ngày sinh. " +
                "Phân nhóm: cá nhân trong nước (groupType=1) / nước ngoài (groupType=3) theo độ tuổi, hoặc doanh nghiệp (groupType=2) theo option set.")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = OptionSetValueControllerExamples.MDEALER_EXAMPLE)))
})
public @interface ApiGetSubObject {
}
