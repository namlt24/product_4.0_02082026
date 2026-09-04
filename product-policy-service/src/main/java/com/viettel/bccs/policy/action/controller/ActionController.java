package com.viettel.bccs.policy.action.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.policy.action.service.ActionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-policy-service/v1/action")
@RequiredArgsConstructor
public class ActionController {
    //
    private final ActionService service;


}