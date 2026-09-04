package com.viettel.bccs.policy.mapping.openapi;

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
@Operation(operationId = "getSaleServiceCode",
        summary = "Tìm mã dịch vụ bán hàng theo dịch vụ viễn thông, lý do, mã gói cước, mã hành động",
        description = "Trả về SALE_SERVICE_CODE trong bảng MAPPING khớp reasonId, telecomServiceId"
                + " (hoặc mapping không ràng buộc dịch vụ), productCode (hoặc mapping không ràng buộc gói cước)"
                + " và actionCode (qua reason_type). Trả về null nếu reasonId null/0 hoặc không tìm thấy."
                + " Phục vụ product-catalog-service (API getListStockTypeWS).")
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                        examples = @ExampleObject(name = "success",
                                value = MappingControllerExamples.SALE_SERVICE_CODE_EXAMPLE)))
})
public @interface ApiGetSaleServiceCode {
}
