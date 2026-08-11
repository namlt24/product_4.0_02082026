package com.viettel.bccs.policy.mapbusinessskipdebt.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.request.SearchSkipDebtRequest;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.response.MapBusinessSkipDebtResponse;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.response.SkipDebtResultResponse;
import com.viettel.bccs.policy.mapbusinessskipdebt.service.MapBusinessSkipDebtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.viettel.bccs.policy.mapbusinessskipdebt.openapi.MapBusinessSkipDebtControllerExamples.*;

@Tag(name = "MapBusinessSkipDebt", description = "APIs quản lý cấu hình bỏ qua công nợ kinh doanh")
@RestController
@RequestMapping("/product-policy-service/v1/map-business-skip-debt")
@RequiredArgsConstructor
@Validated
public class MapBusinessSkipDebtController {

    private final MapBusinessSkipDebtService service;

    @Operation(operationId = "findAllMapBusinessSkipDebt", summary = "Lấy tất cả cấu hình bỏ qua công nợ",
            description = "Trả về toàn bộ bản ghi trong bảng MAP_BUSINESS_SKIP_DEBT, không phân trang, không lọc.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = LIST_EXAMPLE)))
    })
    @GetMapping("/findAll")
    public StandardResponse<List<MapBusinessSkipDebtResponse>> findAll() {
        return StandardResponses.success(service.findAll());
    }

    @Operation(operationId = "findMapBusinessSkipDebtById", summary = "Lấy cấu hình bỏ qua công nợ theo ID",
            description = "Tra cứu 1 bản ghi MAP_BUSINESS_SKIP_DEBT theo MAP_ID (khoá chính).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SINGLE_EXAMPLE)))
    })
    @GetMapping("/findById/{mapId}")
    public StandardResponse<MapBusinessSkipDebtResponse> findById(
            @Parameter(description = "ID bản ghi", example = "1")
            @PathVariable
            @Min(value = 0, message = "mapId phải >= 0")
            @Max(value = 9999999999L, message = "mapId vượt quá độ dài cột (precision 10)")
            Long mapId) {
        return StandardResponses.success(service.findById(mapId));
    }

    @Operation(operationId = "findActiveMapBusinessSkipDebtByActionCodeAndTelecomServiceId",
            summary = "Tìm cấu hình đang hiệu lực theo mã hành động và dịch vụ viễn thông",
            description = "Trả về danh sách MAP_BUSINESS_SKIP_DEBT đang hiệu lực khớp ACTION_CODE và TELECOM_SERVICE_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = LIST_EXAMPLE)))
    })
    @GetMapping("/findActiveByActionCodeAndTelecomServiceId")
    public StandardResponse<List<MapBusinessSkipDebtResponse>> findActiveByActionCodeAndTelecomServiceId(
            @Parameter(description = "Mã hành động", example = "ACT001")
            @RequestParam(required = false)
            @Size(max = 10, message = "actionCode tối đa 10 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9]{0,10}$", message = "actionCode chỉ gồm chữ và số")
            String actionCode,
            @Parameter(description = "ID dịch vụ viễn thông", example = "100")
            @RequestParam(required = false)
            @Min(value = 0, message = "telecomServiceId phải >= 0")
            @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
            Long telecomServiceId) {
        return StandardResponses.success(
                service.findActiveByActionCodeAndTelecomServiceId(actionCode, telecomServiceId));
    }

    @Operation(operationId = "findActiveMapBusinessSkipDebtByShopId", summary = "Tìm cấu hình đang hiệu lực theo ID cửa hàng",
            description = "Trả về danh sách MAP_BUSINESS_SKIP_DEBT đang hiệu lực khớp SHOP_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = LIST_EXAMPLE)))
    })
    @GetMapping("/findActiveByShopId/{shopId}")
    public StandardResponse<List<MapBusinessSkipDebtResponse>> findActiveByShopId(
            @Parameter(description = "ID cửa hàng", example = "10")
            @PathVariable
            @Min(value = 0, message = "shopId phải >= 0")
            @Max(value = 9999999999L, message = "shopId vượt quá độ dài cột (precision 10)")
            Long shopId) {
        return StandardResponses.success(service.findActiveByShopId(shopId));
    }

    @Operation(operationId = "findActiveMapBusinessSkipDebtByStaffId", summary = "Tìm cấu hình đang hiệu lực theo ID nhân viên",
            description = "Trả về danh sách MAP_BUSINESS_SKIP_DEBT đang hiệu lực khớp STAFF_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = LIST_EXAMPLE)))
    })
    @GetMapping("/findActiveByStaffId/{staffId}")
    public StandardResponse<List<MapBusinessSkipDebtResponse>> findActiveByStaffId(
            @Parameter(description = "ID nhân viên", example = "20")
            @PathVariable
            @Min(value = 0, message = "staffId phải >= 0")
            @Max(value = 9999999999L, message = "staffId vượt quá độ dài cột (precision 10)")
            Long staffId) {
        return StandardResponses.success(service.findActiveByStaffId(staffId));
    }

    @Operation(
            operationId = "API_POLICY_SKIP_DEBT_SEARCH",
            summary = "Tra cứu quy tắc miễn công nợ kinh doanh",
            description = "Tìm kiếm bản ghi MAP_BUSINESS_SKIP_DEBT thỏa mãn đồng thời: đúng mã hành động, đúng dịch vụ viễn thông, trong khoảng thời gian hiệu lực, thuộc đại lý/nhân viên hợp lệ và đang hoạt động, và khớp mã số thuê bao/doanh nghiệp"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sách kết quả hoặc danh sách rỗng",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SEARCH_EXAMPLE)))
    })
    @PostMapping("/searchForAPI")
    public StandardResponse<List<SkipDebtResultResponse>> searchForAPI(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin tra cứu",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = SearchSkipDebtRequest.class)
                    ))
            @Valid @RequestBody SearchSkipDebtRequest request) {
        return StandardResponses.success(service.searchForAPI(request));
    }
}
