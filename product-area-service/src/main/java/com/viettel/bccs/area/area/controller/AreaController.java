package com.viettel.bccs.area.area.controller;

import com.viettel.bccs.area.area.dto.response.AreaResponse;
import com.viettel.bccs.area.area.service.AreaService;
import com.viettel.bccs.area.utils.RequestValidator;
import com.viettel.bccs.area.utils.ValidationPatterns;
import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.viettel.bccs.area.area.openapi.AreaControllerExamples.*;

import java.util.List;

@RestController
@RequestMapping("/product-area-service/v1/area")
@RequiredArgsConstructor
@Tag(name = "Area", description = "Tra cứu địa bàn hành chính (tỉnh/quận/phường)")
public class AreaController {

    private final AreaService areaService;

    @Operation(operationId = "getAllArea", summary = "Lấy toàn bộ địa bàn",
            description = "Trả về danh sách toàn bộ bản ghi trong bảng AREA, không phân trang, không lọc.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = AREA_LIST_EXAMPLE)))
    })
    @GetMapping("/getAll")
    public StandardResponse<List<AreaResponse>> getAll() {
        return StandardResponses.success(areaService.getAll());
    }

    @Operation(operationId = "getAreaByCode", summary = "Lấy địa bàn theo mã",
            description = "Tra cứu 1 bản ghi AREA theo AREA_CODE (khoá chính). Ví dụ dùng dữ liệu mẫu 'A076' (An Giang) đã có sẵn trong DB.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = AREA_SINGLE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy địa bàn với mã tương ứng")
    })
    @GetMapping("/getByAreaCode/{areaCode}")
    public StandardResponse<AreaResponse> getByAreaCode(
            @Parameter(description = "Mã địa bàn (AREA_CODE)", example = "A076003005001", required = true)
            @PathVariable
            String areaCode) {
        RequestValidator.requireNotBlank(areaCode, "areaCode", "BCCS-AREA-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(areaCode, "areaCode", 200, "BCCS-AREA-VALIDATE-SIZE");
        RequestValidator.checkPattern(areaCode, "areaCode", ValidationPatterns.ALPHANUMERIC, "BCCS-AREA-VALIDATE-PATTERN");
        return StandardResponses.success(areaService.getByAreaCode(areaCode));
    }

    @Operation(operationId = "getAreaByParentCode", summary = "Lấy danh sách địa bàn con theo mã cha",
            description = "Tra cứu các bản ghi AREA có PARENT_CODE khớp tham số. Ví dụ dùng 'A076' (An Giang) trả về các quận/huyện trực thuộc.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = AREA_LIST_EXAMPLE)))
    })
    @GetMapping("/getByParentCode/{parentCode}")
    public StandardResponse<List<AreaResponse>> getByParentCode(
            @Parameter(description = "Mã địa bàn cha (PARENT_CODE)", example = "A076", required = true)
            @PathVariable
            String parentCode) {
        RequestValidator.checkMaxLength(parentCode, "parentCode", 200, "BCCS-AREA-VALIDATE-SIZE");
        RequestValidator.checkPattern(parentCode, "parentCode", ValidationPatterns.ALPHANUMERIC, "BCCS-AREA-VALIDATE-PATTERN");
        return StandardResponses.success(areaService.getByParentCode(parentCode));
    }

    @Operation(operationId = "getAreaByProvince", summary = "Lấy danh sách địa bàn theo mã tỉnh/thành",
            description = "Tra cứu các bản ghi AREA theo cột PROVINCE. Ví dụ dùng 'A076' (An Giang).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = AREA_LIST_EXAMPLE)))
    })
    @GetMapping("/getByProvince")
    public StandardResponse<List<AreaResponse>> getByProvince(
            @Parameter(description = "Mã tỉnh/thành (PROVINCE)", example = "A076", required = true)
            @RequestParam(required = false)
            String province) {
        RequestValidator.requireNotBlank(province, "province", "BCCS-AREA-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(province, "province", 50, "BCCS-AREA-VALIDATE-SIZE");
        RequestValidator.checkPattern(province, "province", ValidationPatterns.ALPHANUMERIC, "BCCS-AREA-VALIDATE-PATTERN");
        return StandardResponses.success(areaService.getByProvince(province));
    }
}
