package com.viettel.bccs.productcatalog.productofferrelationdetail.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.productcatalog.productofferrelationdetail.dto.response.ProductOfferRelationDetailResponse;
import com.viettel.bccs.productcatalog.productofferrelationdetail.service.ProductOfferRelationDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product-catalog-service/v1/productofferrelationdetail")
@RequiredArgsConstructor
public class ProductOfferRelationDetailController {

    private final ProductOfferRelationDetailService productOfferRelationDetailService;

}