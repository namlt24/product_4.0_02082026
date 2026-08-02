package com.viettel.bccs.productcatalog.productofferrelation.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.productcatalog.productofferrelation.dto.response.ProductOfferRelationResponse;
import com.viettel.bccs.productcatalog.productofferrelation.service.ProductOfferRelationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product-catalog-service/v1/productofferrelation")
@RequiredArgsConstructor
public class ProductOfferRelationController {

    private final ProductOfferRelationService productOfferRelationService;

}