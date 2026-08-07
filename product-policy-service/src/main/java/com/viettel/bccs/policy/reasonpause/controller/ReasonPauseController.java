package com.viettel.bccs.policy.reasonpause.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.reasonpause.dto.response.ReasonPauseDTO;
import com.viettel.bccs.policy.reasonpause.service.ReasonPauseService;
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
    public StandardResponse<ReasonPauseDTO> findById(@PathVariable Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @GetMapping("/getReasonPauseByReasonId/{reasonId}")
    public StandardResponse<List<ReasonPauseDTO>> getReasonPauseByReasonId(@PathVariable Long reasonId) {
        return StandardResponses.success(service.getReasonPauseByReasonId(reasonId));
    }
}
