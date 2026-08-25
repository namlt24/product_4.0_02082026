package com.viettel.bccs.policy.action.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.action.dto.response.ActionResponse;
import com.viettel.bccs.policy.action.service.ActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product-policy-service/v1/action")
@RequiredArgsConstructor
public class ActionController {
    //
    private final ActionService service;


}