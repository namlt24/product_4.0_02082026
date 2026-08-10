package com.viettel.bccs.policy.mapactiveinfo.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;

import com.viettel.bccs.policy.mapactiveinfo.dto.request.ChanelTypeIdRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoQuerryService;
import io.swagger.v3.oas.annotations.Operation;
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
    public StandardResponse<MapActiveInfoResponse> findById(@PathVariable Long id) {
        return StandardResponses.success(mapActiveInfoQuerryService.findById(id));
    }

    @PostMapping("/getChanelTypeIdMapActiveInfo")
    @Operation(operationId = "getChanelTypeIdMapActiveInfo",
            summary = "Lấy channelTypeId dùng cho map active info",
            description = "Suy ra channelTypeId áp dụng cho nghiệp vụ map active info từ shopChanelTypeId/channelTypeId/pointOfSale của nhân viên.")
    public StandardResponse<Long> getChanelTypeIdMapActiveInfo(@RequestBody ChanelTypeIdRequest request) {
        return StandardResponses.success(mapActiveInfoQuerryService.getChanelTypeIdMapActiveInfo(request));
    }
}
