package com.viettel.bccs.policy.mapbusinessskipdebt.openapi;

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
@Operation(
        operationId = "API_POLICY_SKIP_DEBT_SEARCH",
        summary = "Tra cứu quy tắc miễn công nợ kinh doanh",
        description = "Tìm kiếm bản ghi MAP_BUSINESS_SKIP_DEBT thỏa mãn đồng thời: đúng mã hành động, đúng dịch vụ viễn thông, trong khoảng thời gian hiệu lực, thuộc đại lý/nhân viên hợp lệ và đang hoạt động, và khớp mã số thuê bao/doanh nghiệp"
)
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Danh sách kết quả hoặc danh sách rỗng",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success", value = MapBusinessSkipDebtControllerExamples.SEARCH_EXAMPLE)))
})
public @interface ApiSearchForAPI {
}
