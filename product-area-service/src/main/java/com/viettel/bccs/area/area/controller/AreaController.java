package com.viettel.bccs.area.area.controller;

import com.viettel.bccs.area.area.dto.response.AreaResponse;
import com.viettel.bccs.area.area.service.AreaService;
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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-area-service/v1/area")
@RequiredArgsConstructor
@Validated
@Tag(name = "Area", description = "Tra cứu địa bàn hành chính (tỉnh/quận/phường)")
public class AreaController {

    private final AreaService areaService;

    private static final String AREA_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "areaCode": "A076003",
                  "parentCode": "A076",
                  "areaGroup": "01",
                  "province": "A076",
                  "district": "A076003",
                  "precinct": null,
                  "streetBlock": null,
                  "name": "An Giang",
                  "fullName": "Tinh An Giang",
                  "center": "1",
                  "pstnCode": "0296",
                  "provinceCode": "A076",
                  "status": "1",
                  "createUser": "system",
                  "createDatetime": "2026-08-11T00:00:00.000+00:00",
                  "updateUser": null,
                  "updateDatetime": null,
                  "regionId": null,
                  "vtMapCode": null,
                  "square": null,
                  "population": null,
                  "households": null,
                  "areaType": null,
                  "vnCode": null,
                  "vnName": null,
                  "isNew": null
                }
              ]
            }""";

    private static final String AREA_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "areaCode": "A076",
                "parentCode": null,
                "areaGroup": "01",
                "province": "A076",
                "district": null,
                "precinct": null,
                "streetBlock": null,
                "name": "An Giang",
                "fullName": "Tinh An Giang",
                "center": "1",
                "pstnCode": "0296",
                "provinceCode": "A076",
                "status": "1",
                "createUser": "system",
                "createDatetime": "2026-08-11T00:00:00.000+00:00",
                "updateUser": null,
                "updateDatetime": null,
                "regionId": null,
                "vtMapCode": null,
                "square": null,
                "population": null,
                "households": null,
                "areaType": null,
                "vnCode": null,
                "vnName": null,
                "isNew": null
              }
            }""";

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
            @Size(min = 1, max = 200, message = "areaCode tối đa 200 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9]{1,200}$", message = "areaCode chỉ gồm chữ và số")
            String areaCode) {
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
            @Size(max = 200, message = "parentCode tối đa 200 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9]{0,200}$", message = "parentCode chỉ gồm chữ và số")
            String parentCode) {
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
            @RequestParam
            @Size(max = 50, message = "province tối đa 50 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "province chỉ gồm chữ và số")
            String province) {
        return StandardResponses.success(areaService.getByProvince(province));
    }
}
