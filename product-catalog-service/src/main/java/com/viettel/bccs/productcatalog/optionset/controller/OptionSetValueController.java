package com.viettel.bccs.productcatalog.optionset.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.optionset.dto.response.GetSubObjectResponse;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetValueService;
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
import java.util.Map;

import static com.viettel.bccs.productcatalog.optionset.openapi.OptionSetValueControllerExamples.*;

@RestController
@RequestMapping("/product-catalog-service/v1/optionsetvalue")
@RequiredArgsConstructor
@Validated
@Tag(name = "OptionSetValue", description = "Tra cứu giá trị của nhóm option set (danh mục dùng chung)")
public class OptionSetValueController {

    private final OptionSetValueService optionSetValueService;

    @Operation(operationId = "getOptionSetValueByOptionSetId", summary = "Lấy danh sách giá trị theo id nhóm option set",
            description = "Tra cứu các bản ghi OPTION_SET_VALUE có OPTION_SET_ID khớp tham số.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = OPTION_SET_VALUE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByOptionSetId")
    public StandardResponse<List<OptionSetValueResponse>> getByOptionSetId(
            @Parameter(description = "Id nhóm option set (OPTION_SET_ID)", example = "1", required = true)
            @RequestParam
            @Min(value = 0, message = "optionSetId phải >= 0")
            @Max(value = 9999999999L, message = "optionSetId vượt quá độ dài cột (precision 10)")
            Long optionSetId) {
        return StandardResponses.success(optionSetValueService.getByOptionSetId(optionSetId));
    }

    @Operation(operationId = "getOptionSetValueByOptionSetIdAndStatus", summary = "Lấy danh sách giá trị theo id nhóm option set và trạng thái",
            description = "Tra cứu các bản ghi OPTION_SET_VALUE có OPTION_SET_ID và STATUS khớp tham số.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = OPTION_SET_VALUE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByOptionSetIdAndStatus")
    public StandardResponse<List<OptionSetValueResponse>> getByOptionSetIdAndStatus(
            @Parameter(description = "Id nhóm option set (OPTION_SET_ID)", example = "1", required = true)
            @RequestParam
            @Min(value = 0, message = "optionSetId phải >= 0")
            @Max(value = 9999999999L, message = "optionSetId vượt quá độ dài cột (precision 10)")
            Long optionSetId,
            @Parameter(description = "Trạng thái (0/1)", example = "1", required = true)
            @RequestParam
            @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
            @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
            String status) {
        return StandardResponses.success(optionSetValueService.getByOptionSetIdAndStatus(optionSetId, status));
    }

    @Operation(operationId = "findOptionSetValueByOptionSetCode", summary = "Lấy danh sách giá trị theo mã nhóm option set",
            description = "Tra cứu các bản ghi OPTION_SET_VALUE có mã nhóm option set khớp tham số.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = OPTION_SET_VALUE_LIST_EXAMPLE)))
    })
    @GetMapping("/findByOptionSetCode/{code}")
    public StandardResponse<List<OptionSetValueResponse>> findByOptionSetCode(
            @Parameter(description = "Mã nhóm option set", example = "CUST_TYPE_GROUP_TYPE", required = true)
            @PathVariable
            @Size(min = 1, max = 100, message = "code tối đa 100 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,100}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
            String code) {
        return StandardResponses.success(optionSetValueService.findByOptionSetCode(code));
    }

    @Operation(operationId = "findOptionSetValueByOptionSetCodes", summary = "Lấy danh sách giá trị theo nhiều mã nhóm option set",
            description = "Tra cứu các bản ghi OPTION_SET_VALUE theo danh sách mã nhóm option set, kết quả gom nhóm theo mã.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = OPTION_SET_VALUE_MAP_EXAMPLE)))
    })
    @GetMapping("/findByOptionSetCodes")
    public StandardResponse<Map<String, List<OptionSetValueResponse>>> findByOptionSetCodes(
            @Parameter(description = "Danh sách mã nhóm option set", example = "[\"CUST_TYPE_GROUP_TYPE\"]", required = true)
            @RequestParam
            @Size(min = 1, max = 100, message = "codes tối đa 100 phần tử")
            List<@Size(min = 1, max = 100, message = "code tối đa 100 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,100}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'") String> codes) {
        return StandardResponses.success(optionSetValueService.findByOptionSetCodes(codes));
    }

    @Operation(operationId = "API_PRODUCT_005",
            summary = "API lấy danh sách nhóm loại khách hàng/ loại khách hàng/ loại giấy tờ",
            description = "API lấy danh sách nhóm loại khách hàng/ loại khách hàng/ loại giấy tờ")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = OPTION_SET_VALUE_LIST_EXAMPLE)))
    })
    @GetMapping("/getAllGroupCustType")
    public StandardResponse<List<OptionSetValueResponse>> getAllGroupCustType() {
        return StandardResponses.success(optionSetValueService.getAllGroupCustType());
    }

    @Operation(operationId = "getValueByTwoCodeOption", summary = "Lấy giá trị option set theo mã option set và tên",
            description = "Tra cứu VALUE của 1 bản ghi OPTION_SET_VALUE theo mã nhóm option set (optSetCode) và tên (name).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = STRING_VALUE_EXAMPLE)))
    })
    @GetMapping("/getValueByTwoCodeOption")
    public StandardResponse<String> getValueByTwoCodeOption(
            @Parameter(description = "Mã nhóm option set", example = "CUST_TYPE_GROUP_TYPE", required = true)
            @RequestParam
            @Size(min = 1, max = 100, message = "optSetCode tối đa 100 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,100}$", message = "optSetCode chỉ gồm chữ, số, '_' hoặc '-'")
            String optSetCode,
            @Parameter(description = "Tên giá trị option set", example = "Cá nhân", required = true)
            @RequestParam
            @Size(min = 1, max = 512, message = "name tối đa 512 ký tự")
            @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{1,512}$", message = "name không được chứa ký tự điều khiển")
            String name) {
        return StandardResponses.success(optionSetValueService.getValueByTwoCodeOption(optSetCode, name));
    }

    @Operation(operationId = "API_PRODUCT_014",
            summary = "API lấy danh sách đối tượng con",
            description = "API lấy danh sách đối tượng con (sub-object) phục vụ MDealer, dựa trên loại khách hàng và ngày sinh. " +
                    "Phân nhóm: cá nhân trong nước (groupType=1) / nước ngoài (groupType=3) theo độ tuổi, hoặc doanh nghiệp (groupType=2) theo option set.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = MDEALER_EXAMPLE)))
    })
    @PostMapping("/getSubObject")
    public StandardResponse<GetSubObjectResponse> getSubObject(
            @Parameter(description = "Loại khách hàng (custType)", example = "PREPAID", required = true)
            @RequestParam
            @Schema(description = "Loại khách hàng", maxLength = 10)
            @Size(min = 1, max = 10, message = "custType tối đa 10 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,10}$", message = "custType chỉ gồm chữ, số, '_' hoặc '-'")
            String custType,

            @Parameter(description = "Ngày sinh khách hàng (ddMMyyyy)", example = "01011990")
            @RequestParam(required = false)
            @Schema(description = "Ngày sinh KH (ddMMyyyy)", maxLength = 8)
            @Pattern(regexp = "^(0[1-9]|[12][0-9]|3[01])(0[1-9]|1[0-2])\\d{4}$",
                    message = "Date must be in the format ddMMyyyy")
            String birthDate) {
        return StandardResponses.success(optionSetValueService.getSubObject(custType, birthDate));
    }
}
