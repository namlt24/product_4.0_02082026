package com.viettel.bccs.policy.mapbusinessskipdebt.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.request.SearchSkipDebtRequest;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.response.MapBusinessSkipDebtResponse;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.response.SkipDebtResultResponse;
import com.viettel.bccs.policy.mapbusinessskipdebt.service.MapBusinessSkipDebtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "MapBusinessSkipDebt", description = "APIs quản lý cấu hình bỏ qua công nợ kinh doanh")
@RestController
@RequestMapping("/product-policy-service/v1/map-business-skip-debt")
@RequiredArgsConstructor
public class MapBusinessSkipDebtController {

    private final MapBusinessSkipDebtService service;

    @Operation(summary = "Lấy tất cả cấu hình bỏ qua công nợ")
    @GetMapping("/findAll")
    public StandardResponse<List<MapBusinessSkipDebtResponse>> findAll() {
        return StandardResponses.success(service.findAll());
    }

    @Operation(summary = "Lấy cấu hình bỏ qua công nợ theo ID")
    @GetMapping("/findById/{mapId}")
    public StandardResponse<MapBusinessSkipDebtResponse> findById(
            @Parameter(description = "ID bản ghi", example = "1")
            @PathVariable Long mapId) {
        return StandardResponses.success(service.findById(mapId));
    }

    @Operation(summary = "Tìm cấu hình đang hiệu lực theo mã hành động và dịch vụ viễn thông")
    @GetMapping("/findActiveByActionCodeAndTelecomServiceId")
    public StandardResponse<List<MapBusinessSkipDebtResponse>> findActiveByActionCodeAndTelecomServiceId(
            @Parameter(description = "Mã hành động", example = "ACT001")
            @RequestParam(required = false) String actionCode,
            @Parameter(description = "ID dịch vụ viễn thông", example = "100")
            @RequestParam(required = false) Long telecomServiceId) {
        return StandardResponses.success(
                service.findActiveByActionCodeAndTelecomServiceId(actionCode, telecomServiceId));
    }

    @Operation(summary = "Tìm cấu hình đang hiệu lực theo ID cửa hàng")
    @GetMapping("/findActiveByShopId/{shopId}")
    public StandardResponse<List<MapBusinessSkipDebtResponse>> findActiveByShopId(
            @Parameter(description = "ID cửa hàng", example = "10")
            @PathVariable Long shopId) {
        return StandardResponses.success(service.findActiveByShopId(shopId));
    }

    @Operation(summary = "Tìm cấu hình đang hiệu lực theo ID nhân viên")
    @GetMapping("/findActiveByStaffId/{staffId}")
    public StandardResponse<List<MapBusinessSkipDebtResponse>> findActiveByStaffId(
            @Parameter(description = "ID nhân viên", example = "20")
            @PathVariable Long staffId) {
        return StandardResponses.success(service.findActiveByStaffId(staffId));
    }

    @Operation(
            operationId = "API_POLICY_SKIP_DEBT_SEARCH",
            summary = "Tra cứu quy tắc miễn công nợ kinh doanh",
            description = "Tìm kiếm bản ghi MAP_BUSINESS_SKIP_DEBT thỏa mãn đồng thời: đúng mã hành động, đúng dịch vụ viễn thông, trong khoảng thời gian hiệu lực, thuộc đại lý/nhân viên hợp lệ và đang hoạt động, và khớp mã số thuê bao/doanh nghiệp"
    )
    @ApiResponse(responseCode = "200", description = "Danh sách kết quả hoặc danh sách rỗng")
    @PostMapping("/searchForAPI")
    public StandardResponse<List<SkipDebtResultResponse>> searchForAPI(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Thông tin tra cứu",
                    required = true,
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            schema = @Schema(implementation = SearchSkipDebtRequest.class)
                    ))
            @RequestBody SearchSkipDebtRequest request) {
        return StandardResponses.success(service.searchForAPI(request));
    }
}