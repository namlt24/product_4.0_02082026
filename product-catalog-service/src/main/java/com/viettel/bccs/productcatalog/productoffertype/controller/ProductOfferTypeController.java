package com.viettel.bccs.productcatalog.productoffertype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productoffertype.dto.response.ProductOfferTypeDTO;
import com.viettel.bccs.productcatalog.productoffertype.service.ProductOfferTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Product Offer Type", description = "APIs quản lý loại mặt hàng")
@RestController
@RequestMapping("/product-catalog-service/v1/product-offer-type")
@RequiredArgsConstructor
public class ProductOfferTypeController {

    private final ProductOfferTypeService service;


}