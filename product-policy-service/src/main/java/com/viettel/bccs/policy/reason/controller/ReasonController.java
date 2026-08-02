package com.viettel.bccs.policy.reason.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
import com.viettel.bccs.policy.reason.service.ReasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product-policy-service/v1/reason")
@RequiredArgsConstructor
public class ReasonController {

    private final ReasonService service;

    @GetMapping("/findById/{id}")
    public StandardResponse<ReasonResponse> findById(@PathVariable Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @GetMapping("/getListReasonByActionCodeAndTelServiceForAudit")
    public StandardResponse<List<ReasonResponse>> getListReasonByActionCodeAndTelServiceForAudit(
            @RequestParam String actionCode,
            @RequestParam Long telServiceId,
            @RequestParam String payType) {
        return StandardResponses.success(service.getListReasonByActionCodeAndTelServiceForAudit(actionCode, telServiceId, payType));
    }
}