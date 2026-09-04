package com.viettel.bccs.policy.ref.refproductpackagefee.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.policy.ref.refproductpackagefee.service.RefProductPackageFeeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/organization-resource-service/v1/ref-product-package-fee")
@RequiredArgsConstructor
public class RefProductPackageFeeController {

    private final RefProductPackageFeeService refProductPackageFeeService;


}