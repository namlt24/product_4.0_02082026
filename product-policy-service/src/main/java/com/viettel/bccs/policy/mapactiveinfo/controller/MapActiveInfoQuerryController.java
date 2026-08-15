package com.viettel.bccs.policy.mapactiveinfo.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;

import com.viettel.bccs.policy.mapactiveinfo.dto.request.ChanelTypeIdRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.viettel.bccs.policy.mapactiveinfo.openapi.MapActiveInfoQuerryControllerExamples.*;

@RestController
@RequestMapping("/product-policy-service/v1/map-active-info")
@RequiredArgsConstructor
@Validated
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
            @Min(value = 1, message = "id phải >= 1")
            @Max(value = 9999999999L, message = "id vượt quá độ dài cho phép")
            Long id) {
        return StandardResponses.success(mapActiveInfoQuerryService.findById(id));
    }

    @PostMapping("/getChanelTypeIdMapActiveInfo")
    @Operation(operationId = "getChanelTypeIdMapActiveInfo",
            summary = "Lấy channelTypeId dùng cho map active info",
            description = "Suy ra channelTypeId áp dụng cho nghiệp vụ map active info từ staffId — server tự resolve "
                    + "thông tin nhân viên/shop (channelTypeId, shopChanelTypeId, pointOfSale) từ organization-resource-service "
                    + "thay vì nhận trực tiếp từ client.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = ChanelTypeIdRequest.class),
                            examples = @ExampleObject(name = "request", value = CHANEL_TYPE_ID_REQUEST_EXAMPLE))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = CHANEL_TYPE_ID_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên với staffId tương ứng")
    })
    public StandardResponse<Long> getChanelTypeIdMapActiveInfo(@Valid @RequestBody ChanelTypeIdRequest request) {
        return StandardResponses.success(mapActiveInfoQuerryService.getChanelTypeIdMapActiveInfo(request));
    }

    @PostMapping("/reindexElasticsearch")
    @Operation(operationId = "reindexElasticsearch",
            summary = "Đồng bộ toàn bộ MAP_ACTIVE_INFO từ Oracle sang Elasticsearch",
            description = "Đọc toàn bộ bảng MAP_ACTIVE_INFO (phân trang) và bulk-index sang Elasticsearch. "
                    + "Dùng để nạp/đồng bộ thủ công cho local/thử nghiệm - service không có write-path ghi "
                    + "MAP_ACTIVE_INFO nên không tự động đồng bộ real-time. Yêu cầu bccs.elasticsearch.enabled=true, "
                    + "nếu không sẽ trả lỗi BCCS-POLICY-MAPACTIVE-0021.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công - trả về số document đã index",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = REINDEX_ELASTICSEARCH_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "Elasticsearch chưa được bật hoặc chưa kết nối được")
    })
    public StandardResponse<Long> reindexElasticsearch() {
        return StandardResponses.success(mapActiveInfoQuerryService.reindexElasticsearch());
    }

    @PostMapping("/searchElasticsearch")
    @Operation(operationId = "searchElasticsearch",
            summary = "[Debug/kiểm thử] Search MAP_ACTIVE_INFO trực tiếp qua Elasticsearch",
            description = "Gọi thẳng MapActiveInfoElasticsearchService.search(...) (không qua nhánh searchCache nội bộ) "
                    + "- dùng để so sánh kết quả với API query Oracle thông thường khi kiểm thử. Yêu cầu đã "
                    + "reindexElasticsearch trước đó và bccs.elasticsearch.enabled=true.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = MapActiveInfoDTO.class))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SEARCH_ELASTICSEARCH_EXAMPLE))),
            @ApiResponse(responseCode = "503", description = "Elasticsearch chưa được bật hoặc chưa kết nối được")
    })
    public StandardResponse<java.util.List<MapActiveInfoDTO>> searchElasticsearch(
            @RequestBody MapActiveInfoDTO example,
            @Parameter(description = "true: lọc cả productCode/regReasonId/promCode (giống searchInvidualField=true trong buildQuery gốc)")
            @RequestParam(defaultValue = "true") boolean searchInvidualField) {
        return StandardResponses.success(mapActiveInfoQuerryService.searchByElasticsearch(example, searchInvidualField));
    }

    @PostMapping("/searchOracle")
    @Operation(operationId = "searchOracle",
            summary = "[Debug/kiểm thử] Search MAP_ACTIVE_INFO trực tiếp qua Oracle",
            description = "Cùng cơ chế HTTP/mapper với searchElasticsearch, chỉ khác nguồn dữ liệu (Oracle thay vì "
                    + "Elasticsearch) - dùng để đo/so sánh hiệu năng công bằng giữa 2 đường.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = MapActiveInfoDTO.class))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SEARCH_ELASTICSEARCH_EXAMPLE)))
    })
    public StandardResponse<java.util.List<MapActiveInfoDTO>> searchOracle(
            @RequestBody MapActiveInfoDTO example,
            @RequestParam(defaultValue = "true") boolean searchInvidualField) {
        return StandardResponses.success(mapActiveInfoQuerryService.searchByOracle(example, searchInvidualField));
    }
}
