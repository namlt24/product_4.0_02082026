package com.viettel.bccs.policy.ref.refproductpackage.controller;

import com.viettel.bccs.policy.ref.refproductpackage.service.RefProductPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/organization-resource-service/v1/ref-product-package")
@RequiredArgsConstructor
public class RefProductPackageController {

    private final RefProductPackageService refProductPackageService;

}