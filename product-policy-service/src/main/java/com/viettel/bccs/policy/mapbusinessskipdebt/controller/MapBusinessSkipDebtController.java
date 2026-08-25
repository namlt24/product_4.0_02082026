package com.viettel.bccs.policy.mapbusinessskipdebt.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.request.SearchSkipDebtRequest;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.response.MapBusinessSkipDebtResponse;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.response.SkipDebtResultResponse;
import com.viettel.bccs.policy.mapbusinessskipdebt.openapi.ApiFindActiveByActionCodeAndTelecomServiceId;
import com.viettel.bccs.policy.mapbusinessskipdebt.openapi.ApiFindActiveByShopId;
import com.viettel.bccs.policy.mapbusinessskipdebt.openapi.ApiFindActiveByStaffId;
import com.viettel.bccs.policy.mapbusinessskipdebt.openapi.ApiFindAll;
import com.viettel.bccs.policy.mapbusinessskipdebt.openapi.ApiFindById;
import com.viettel.bccs.policy.mapbusinessskipdebt.openapi.ApiSearchForAPI;
import com.viettel.bccs.policy.mapbusinessskipdebt.service.MapBusinessSkipDebtService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
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

    @ApiFindAll
    @GetMapping("/findAll")
    public StandardResponse<List<MapBusinessSkipDebtResponse>> findAll() {
        return StandardResponses.success(service.findAll());
    }

    @ApiFindById
    @GetMapping("/findById/{mapId}")
    public StandardResponse<MapBusinessSkipDebtResponse> findById(
            @Parameter(description = "ID bản ghi", example = "1")
            @PathVariable
            Long mapId) {
        return StandardResponses.success(service.findById(mapId));
    }

    @ApiFindActiveByActionCodeAndTelecomServiceId
    @GetMapping("/findActiveByActionCodeAndTelecomServiceId")
    public StandardResponse<List<MapBusinessSkipDebtResponse>> findActiveByActionCodeAndTelecomServiceId(
            @Parameter(description = "Mã hành động", example = "ACT001")
            @RequestParam(required = false)
            String actionCode,
            @Parameter(description = "ID dịch vụ viễn thông", example = "100")
            @RequestParam(required = false)
            Long telecomServiceId) {
        return StandardResponses.success(
                service.findActiveByActionCodeAndTelecomServiceId(actionCode, telecomServiceId));
    }

    @ApiFindActiveByShopId
    @GetMapping("/findActiveByShopId/{shopId}")
    public StandardResponse<List<MapBusinessSkipDebtResponse>> findActiveByShopId(
            @Parameter(description = "ID cửa hàng", example = "10")
            @PathVariable
            Long shopId) {
        return StandardResponses.success(service.findActiveByShopId(shopId));
    }

    @ApiFindActiveByStaffId
    @GetMapping("/findActiveByStaffId/{staffId}")
    public StandardResponse<List<MapBusinessSkipDebtResponse>> findActiveByStaffId(
            @Parameter(description = "ID nhân viên", example = "20")
            @PathVariable
            Long staffId) {
        return StandardResponses.success(service.findActiveByStaffId(staffId));
    }

    @ApiSearchForAPI
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
