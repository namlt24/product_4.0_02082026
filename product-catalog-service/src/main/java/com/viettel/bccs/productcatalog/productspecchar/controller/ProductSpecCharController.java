package com.viettel.bccs.productcatalog.productspecchar.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productspecchar.dto.response.ProductSpecCharResponse;
import com.viettel.bccs.productcatalog.productspecchar.service.ProductSpecCharService;
import com.viettel.bccs.productcatalog.utils.RequestValidator;
import com.viettel.bccs.productcatalog.utils.ValidationPatterns;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.viettel.bccs.productcatalog.productspecchar.openapi.ProductSpecCharControllerExamples.*;

@RestController
@RequestMapping("/product-catalog-service/v1/productspecchar")
@RequiredArgsConstructor
public class ProductSpecCharController {

    private final ProductSpecCharService productSpecCharService;

    @Operation(operationId = "getAllProductSpecChar", summary = "Lấy toàn bộ thuộc tính sản phẩm",
            description = "Trả về danh sách toàn bộ bản ghi trong bảng PRODUCT_SPEC_CHAR, không phân trang, không lọc.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SPEC_CHAR_LIST_EXAMPLE)))
    })
    @GetMapping("/getAll")
    public StandardResponse<List<ProductSpecCharResponse>> getAll() {
        return StandardResponses.success(productSpecCharService.getAll());
    }

    @Operation(operationId = "getProductSpecCharById", summary = "Lấy thuộc tính sản phẩm theo ID",
            description = "Tra cứu 1 bản ghi PRODUCT_SPEC_CHAR theo khoá chính PRODUCT_SPEC_CHAR_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SPEC_CHAR_SINGLE_EXAMPLE)))
    })
    @GetMapping("/getById/{id}")
    public StandardResponse<ProductSpecCharResponse> getById(
            @Parameter(description = "ID thuộc tính sản phẩm (PRODUCT_SPEC_CHAR_ID)", example = "1", required = true)
            @PathVariable
            Long id) {
        RequestValidator.checkRange(id, "id", 1L, 9999999999L, "BCCS-CATALOG-VALIDATE-RANGE");
        return StandardResponses.success(productSpecCharService.getById(id));
    }

    @Operation(operationId = "getProductSpecCharByCode", summary = "Lấy thuộc tính sản phẩm theo mã",
            description = "Tra cứu 1 bản ghi PRODUCT_SPEC_CHAR theo cột CODE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SPEC_CHAR_SINGLE_EXAMPLE)))
    })
    @GetMapping("/getByCode/{code}")
    public StandardResponse<ProductSpecCharResponse> getByCode(
            @Parameter(description = "Mã thuộc tính sản phẩm (CODE)", example = "COLOR", required = true)
            @PathVariable
            String code) {
        RequestValidator.requireNotBlank(code, "code", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(code, "code", 200, "BCCS-CATALOG-VALIDATE-SIZE");
        RequestValidator.checkPattern(code, "code", ValidationPatterns.CODE, "BCCS-CATALOG-VALIDATE-PATTERN");
        return StandardResponses.success(productSpecCharService.getByCode(code));
    }

    @Operation(operationId = "findProductSpecCharByIds", summary = "Lấy danh sách thuộc tính sản phẩm theo danh sách ID",
            description = "Truy vấn nhiều bản ghi PRODUCT_SPEC_CHAR theo danh sách PRODUCT_SPEC_CHAR_ID truyền vào.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SPEC_CHAR_LIST_EXAMPLE)))
    })
    @PostMapping("/findByIds")
    public StandardResponse<List<ProductSpecCharResponse>> findByIds(
            @Parameter(description = "Danh sách ID thuộc tính sản phẩm", required = true)
            @RequestBody
            List<Long> ids) {
        RequestValidator.requireNotEmpty(ids, "ids", "BCCS-CATALOG-VALIDATE-REQUIRED");
        RequestValidator.checkSize(ids, "ids", 1000, "BCCS-CATALOG-VALIDATE-SIZE");
        return StandardResponses.success(productSpecCharService.findByIds(ids));
    }

}