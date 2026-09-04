package com.viettel.bccs.productcatalog.productpackagecharuse.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.productcatalog.productpackagecharuse.service.ProductPackageCharUseService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Product Package Char Use", description = "APIs quản lý thuộc tính sản phẩm trong gói")
@RestController
@RequestMapping("/product-catalog-service/v1/product-package-char-use")
@RequiredArgsConstructor
public class ProductPackageCharUseController {

    private final ProductPackageCharUseService service;
}