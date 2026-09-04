package com.viettel.bccs.policy.ref.refproductpackage.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.policy.ref.refproductpackage.service.RefProductPackageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/organization-resource-service/v1/ref-product-package")
@RequiredArgsConstructor
public class RefProductPackageController {

    private final RefProductPackageService refProductPackageService;

}