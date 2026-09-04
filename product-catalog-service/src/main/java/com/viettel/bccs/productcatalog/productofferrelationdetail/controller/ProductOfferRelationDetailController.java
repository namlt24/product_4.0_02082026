package com.viettel.bccs.productcatalog.productofferrelationdetail.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.productcatalog.productofferrelationdetail.service.ProductOfferRelationDetailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-catalog-service/v1/productofferrelationdetail")
@RequiredArgsConstructor
public class ProductOfferRelationDetailController {

    private final ProductOfferRelationDetailService productOfferRelationDetailService;

}