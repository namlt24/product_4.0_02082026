package com.viettel.bccs.productcatalog.optionset.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetResponse;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.viettel.bccs.productcatalog.optionset.openapi.OptionSetControllerExamples.*;

@RestController
@RequestMapping("/product-catalog-service/v1/optionset")
@RequiredArgsConstructor
@Validated
@Tag(name = "OptionSet", description = "Tra cứu nhóm option set (danh mục dùng chung)")
public class OptionSetController {

    private final OptionSetService optionSetService;

    @Operation(operationId = "getAllOptionSet", summary = "Lấy toàn bộ nhóm option set",
            description = "Trả về danh sách toàn bộ bản ghi trong bảng OPTION_SET, không phân trang, không lọc.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = OPTION_SET_LIST_EXAMPLE)))
    })
    @GetMapping
    public StandardResponse<List<OptionSetResponse>> getAll() {
        return StandardResponses.success(optionSetService.getAll());
    }

    @Operation(operationId = "getOptionSetById", summary = "Lấy nhóm option set theo id",
            description = "Tra cứu 1 bản ghi OPTION_SET theo OPTION_SET_ID (khoá chính).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = OPTION_SET_SINGLE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhóm option set với id tương ứng")
    })
    @GetMapping("/getById/{id}")
    public StandardResponse<OptionSetResponse> getById(
            @Parameter(description = "Id nhóm option set (OPTION_SET_ID)", example = "1", required = true)
            @PathVariable
            @Min(value = 0, message = "id phải >= 0")
            @Max(value = 9999999999L, message = "id vượt quá độ dài cột (precision 10)")
            Long id) {
        return StandardResponses.success(optionSetService.getById(id));
    }

    @Operation(operationId = "getOptionSetByCode", summary = "Lấy nhóm option set theo mã",
            description = "Tra cứu 1 bản ghi OPTION_SET theo CODE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = OPTION_SET_SINGLE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhóm option set với mã tương ứng")
    })
    @GetMapping("/getByCode/{code}")
    public StandardResponse<OptionSetResponse> getByCode(
            @Parameter(description = "Mã nhóm option set (CODE)", example = "CUST_TYPE_GROUP_TYPE", required = true)
            @PathVariable
            @Size(min = 1, max = 100, message = "code tối đa 100 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,100}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
            String code) {
        return StandardResponses.success(optionSetService.getByCode(code));
    }

}
