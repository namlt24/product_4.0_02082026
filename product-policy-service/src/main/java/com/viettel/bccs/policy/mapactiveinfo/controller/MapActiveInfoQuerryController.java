package com.viettel.bccs.policy.mapactiveinfo.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;

import com.viettel.bccs.policy.mapactiveinfo.dto.request.ChanelTypeIdRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoQuerryService;
import com.viettel.bccs.policy.utils.RequestValidator;
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

import static com.viettel.bccs.policy.mapactiveinfo.openapi.MapActiveInfoQuerryControllerExamples.*;

@RestController
@RequestMapping("/product-policy-service/v1/map-active-info")
@RequiredArgsConstructor
@Tag(name = "Controller querry thông thường")
public class MapActiveInfoQuerryController {
    private final MapActiveInfoQuerryService mapActiveInfoQuerryService;

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
            Long id) {
        RequestValidator.checkRange(id, "id", 1L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
        return StandardResponses.success(mapActiveInfoQuerryService.findById(id));
    }

    @PostMapping("/getChanelTypeIdMapActiveInfo")
    @Operation(operationId = "getChanelTypeIdMapActiveInfo",
            summary = "Lấy channelTypeId dùng cho map active info",
            description = "Thông tin nhân viên/shop (channelTypeId, shopChanelTypeId, pointOfSale)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = ChanelTypeIdRequest.class),
                            examples = @ExampleObject(name = "request", value = CHANEL_TYPE_ID_REQUEST_EXAMPLE))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = CHANEL_TYPE_ID_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên với staffId tương ứng")
    })
    public StandardResponse<Long> getChanelTypeIdMapActiveInfo(@RequestBody ChanelTypeIdRequest request) {
        return StandardResponses.success(mapActiveInfoQuerryService.getChanelTypeIdMapActiveInfo(request));
    }

    @PostMapping("/reindexElasticsearch")
    @Operation(operationId = "reindexElasticsearch",
            summary = "Đồng bộ toàn bộ MAP_ACTIVE_INFO từ Oracle sang Elasticsearch",
            description = "Đọc toàn bộ bảng MAP_ACTIVE_INFO (phân trang) và bulk-index sang Elasticsearch. "
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công - trả về số document đã index",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = REINDEX_ELASTICSEARCH_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "Elasticsearch chưa được bật hoặc chưa kết nối được")
    })
    public StandardResponse<Long> reindexElasticsearch() {
        return StandardResponses.success(mapActiveInfoQuerryService.reindexElasticsearch());
    }
}
