package com.viettel.bccs.policy.mapactiveinfo.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;

import com.viettel.bccs.policy.mapactiveinfo.dto.request.ChanelTypeIdRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.openapi.ApiFindById;
import com.viettel.bccs.policy.mapactiveinfo.openapi.ApiGetChanelTypeIdMapActiveInfo;
import com.viettel.bccs.policy.mapactiveinfo.openapi.ApiReindexElasticsearch;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoQuerryService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product-policy-service/v1/map-active-info")
@RequiredArgsConstructor
@Tag(name = "Controller querry thông thường")
public class MapActiveInfoQuerryController {
    private final MapActiveInfoQuerryService mapActiveInfoQuerryService;

    @GetMapping("/findById/{id}")
    @ApiFindById
    public StandardResponse<MapActiveInfoResponse> findById(
            @Parameter(description = "ID bản ghi MAP_ACTIVE_INFO", example = "1", required = true)
            @PathVariable
            Long id) {
        return StandardResponses.success(mapActiveInfoQuerryService.findById(id));
    }

    @PostMapping("/getChanelTypeIdMapActiveInfo")
    @ApiGetChanelTypeIdMapActiveInfo
    public StandardResponse<Long> getChanelTypeIdMapActiveInfo(@RequestBody ChanelTypeIdRequest request) {
        return StandardResponses.success(mapActiveInfoQuerryService.getChanelTypeIdMapActiveInfo(request));
    }

    @PostMapping("/reindexElasticsearch")
    @ApiReindexElasticsearch
    public StandardResponse<Long> reindexElasticsearch() {
        return StandardResponses.success(mapActiveInfoQuerryService.reindexElasticsearch());
    }
}
