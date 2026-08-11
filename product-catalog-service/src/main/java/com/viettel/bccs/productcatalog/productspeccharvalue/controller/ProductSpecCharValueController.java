package com.viettel.bccs.productcatalog.productspeccharvalue.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productspeccharvalue.dto.response.ProductSpecCharValueResponse;
import com.viettel.bccs.productcatalog.productspeccharvalue.service.ProductSpecCharValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.viettel.bccs.productcatalog.productspeccharvalue.openapi.ProductSpecCharValueControllerExamples.*;

@RestController
@RequestMapping("/product-catalog-service/v1/productspectcharvalue")
@RequiredArgsConstructor
@Validated
public class ProductSpecCharValueController {

    private final ProductSpecCharValueService productSpecCharValueService;

    @Operation(operationId = "findProductSpecCharValueByIds", summary = "Lấy danh sách giá trị thuộc tính sản phẩm theo danh sách ID",
            description = "Truy vấn nhiều bản ghi PRODUCT_SPEC_CHAR_VALUE theo danh sách PRODUCT_SPEC_CHAR_VALUE_ID truyền vào.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SPEC_CHAR_VALUE_LIST_EXAMPLE)))
    })
    @PostMapping("/findByIds")
    public StandardResponse<List<ProductSpecCharValueResponse>> findByIds(
            @Parameter(description = "Danh sách ID giá trị thuộc tính sản phẩm", required = true)
            @RequestBody
            @Size(min = 1, max = 1000, message = "ids tối đa 1000 phần tử")
            List<Long> ids) {
        return StandardResponses.success(productSpecCharValueService.findByIds(ids));
    }

}