package com.viettel.bccs.productcatalog.prodpackproductoffertype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.productcatalog.prodpackproductoffertype.service.ProdPackProductOfferTypeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Prod Pack Product Offer Type", description = "APIs quản lý liên kết gói sản phẩm và loại mặt hàng")
@RestController
@RequestMapping("/product-catalog-service/v1/prod-pack-product-offer-type")
@RequiredArgsConstructor
public class ProdPackProductOfferTypeController {

    private final ProdPackProductOfferTypeService service;
}