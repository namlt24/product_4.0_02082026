package com.viettel.bccs.policy.mapactiveinfo.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;

import com.viettel.bccs.policy.mapactiveinfo.dto.request.ChanelTypeIdRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoQuerryService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product-policy-service/v1/map-active-info")
@RequiredArgsConstructor
@Validated
@Tag(name = "Controller querry thông thường")
public class MapActiveInfoQuerryController {
    private final MapActiveInfoQuerryService mapActiveInfoQuerryService;

    private static final String FIND_BY_ID_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000004",
              "requestId": "req-0004",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "id": 1,
                "telServiceId": 73,
                "productCode": "PROD001",
                "productName": "Dịch vụ Mobifone",
                "regReasonId": 1,
                "reasonName": "Lý do đăng ký mới",
                "promCode": "PROMO001",
                "promName": "Khuyến mãi tháng 1",
                "channelTypeId": 1,
                "channelName": "Kênh 1",
                "provinceCode": "HN",
                "districtCode": "HN001"
              }
            }""";

    private static final String CHANEL_TYPE_ID_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000005",
              "requestId": "req-0005",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": 1
            }""";

    private static final String CHANEL_TYPE_ID_REQUEST_EXAMPLE = """
            {
              "shopChanelTypeId": "1",
              "channelTypeId": "1",
              "pointOfSale": "POS01"
            }""";

    @GetMapping("/findById/{id}")
    @Operation(operationId = "findMapActiveInfoById", summary = "Lấy map active info theo id",
            description = "Tra cứu 1 bản ghi MAP_ACTIVE_INFO theo ID (khoá chính).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = FIND_BY_ID_EXAMPLE)))
    })
    public StandardResponse<MapActiveInfoResponse> findById(
            @Parameter(description = "ID bản ghi MAP_ACTIVE_INFO", example = "1", required = true)
            @PathVariable
            @Min(value = 1, message = "id phải >= 1")
            @Max(value = 9999999999L, message = "id vượt quá độ dài cho phép")
            Long id) {
        return StandardResponses.success(mapActiveInfoQuerryService.findById(id));
    }

    @PostMapping("/getChanelTypeIdMapActiveInfo")
    @Operation(operationId = "getChanelTypeIdMapActiveInfo",
            summary = "Lấy channelTypeId dùng cho map active info",
            description = "Suy ra channelTypeId áp dụng cho nghiệp vụ map active info từ shopChanelTypeId/channelTypeId/pointOfSale của nhân viên.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = ChanelTypeIdRequest.class),
                            examples = @ExampleObject(name = "request", value = CHANEL_TYPE_ID_REQUEST_EXAMPLE))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = CHANEL_TYPE_ID_EXAMPLE)))
    })
    public StandardResponse<Long> getChanelTypeIdMapActiveInfo(@RequestBody ChanelTypeIdRequest request) {
        return StandardResponses.success(mapActiveInfoQuerryService.getChanelTypeIdMapActiveInfo(request));
    }
}
