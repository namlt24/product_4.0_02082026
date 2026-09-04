package com.viettel.bccs.productcatalog.productofferrelation.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.productcatalog.productofferrelation.service.ProductOfferRelationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-catalog-service/v1/productofferrelation")
@RequiredArgsConstructor
public class ProductOfferRelationController {

    private final ProductOfferRelationService productOfferRelationService;

}