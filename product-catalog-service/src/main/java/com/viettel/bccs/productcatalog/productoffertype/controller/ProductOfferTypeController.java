package com.viettel.bccs.productcatalog.productoffertype.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.productcatalog.productoffertype.service.ProductOfferTypeService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Product Offer Type", description = "APIs quản lý loại mặt hàng")
@RestController
@RequestMapping("/product-catalog-service/v1/product-offer-type")
@RequiredArgsConstructor
public class ProductOfferTypeController {

    private final ProductOfferTypeService service;


}