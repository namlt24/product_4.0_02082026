package com.viettel.bccs.policy.mapactiveinfo.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.exception.LogicException;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.IsCheckMapActiveInfoRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.RequestMbccs;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.ValidateInputMapActiveInfoRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.GetProductCodeByMapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.ValidateInputMapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.ValidateMapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoQuerryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
}
