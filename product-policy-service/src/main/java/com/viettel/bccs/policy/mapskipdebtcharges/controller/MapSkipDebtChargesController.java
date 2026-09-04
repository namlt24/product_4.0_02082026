package com.viettel.bccs.policy.mapskipdebtcharges.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapskipdebtcharges.dto.response.MapSkipDebtChargesDTO;
import com.viettel.bccs.policy.mapskipdebtcharges.dto.response.MapSkipDebtChargesDTOFull;
import com.viettel.bccs.policy.mapskipdebtcharges.openapi.ApiGetMapSkipDebtChargeFullInfo;
import com.viettel.bccs.policy.mapskipdebtcharges.service.MapSkipDebtChargesService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-policy-service/v1/map-skip-debt-charges")
@RequiredArgsConstructor
public class MapSkipDebtChargesController {

    private final MapSkipDebtChargesService service;

    @PostMapping("/getMapSkipDebtChargeFullInfo")
    @ApiGetMapSkipDebtChargeFullInfo
    public StandardResponse<List<MapSkipDebtChargesDTOFull>> getMapSkipDebtChargeFullInfo(
            @RequestBody List<MapSkipDebtChargesDTO> mapSkipDebtChargesDtos) throws Exception {
        return StandardResponses.success(service.getFullInfo(mapSkipDebtChargesDtos));
    }
}
