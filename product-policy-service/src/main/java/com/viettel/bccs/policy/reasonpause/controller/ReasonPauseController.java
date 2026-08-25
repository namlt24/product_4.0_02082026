package com.viettel.bccs.policy.reasonpause.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.reasonpause.dto.response.ReasonPauseDTO;
import com.viettel.bccs.policy.reasonpause.openapi.ApiFindById;
import com.viettel.bccs.policy.reasonpause.openapi.ApiGetReasonPauseByReasonId;
import com.viettel.bccs.policy.reasonpause.service.ReasonPauseService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product-policy-service/v1/reason-pause")
@RequiredArgsConstructor
public class ReasonPauseController {

    private final ReasonPauseService service;

    @GetMapping("/findById/{id}")
    @ApiFindById
    public StandardResponse<ReasonPauseDTO> findById(
            @Parameter(description = "ID kỳ tạm ngưng (REASON_PAUSE_ID)", example = "1", required = true)
            @PathVariable
            Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @GetMapping("/getReasonPauseByReasonId/{reasonId}")
    @ApiGetReasonPauseByReasonId
    public StandardResponse<List<ReasonPauseDTO>> getReasonPauseByReasonId(
            @Parameter(description = "ID hình thức hòa mạng (REASON_ID)", example = "1", required = true)
            @PathVariable
            Long reasonId) {
        return StandardResponses.success(service.getReasonPauseByReasonId(reasonId));
    }
}
