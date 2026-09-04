package com.viettel.bccs.productcatalog.product.controller;

import java.util.HashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.product.openapi.ApiFindByTelecomSubTypeOfferTypeCheckProductStatusMap;
import com.viettel.bccs.productcatalog.product.service.ProductOfferingQuerryService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-catalog-service/v1/product")
@RequiredArgsConstructor
@Tag(name = "Product Offering")
public class ProductOfferingQuerryController {

    private final ProductOfferingQuerryService productOfferingQuerryService;

    @GetMapping("/findByTelecomSubTypeOfferTypeCheckProductStatusMap")
    @ApiFindByTelecomSubTypeOfferTypeCheckProductStatusMap
    public StandardResponse<HashMap<Long, String>> findByTelecomSubTypeOfferTypeCheckProductStatusMap(
            @Parameter(example = "1")
            @RequestParam(required = false)
            Long telecomServiceId,
            @Parameter(example = "1")
            @RequestParam(required = false)
            String subType,
            @Parameter(example = "1")
            @RequestParam(required = false)
            Long offerTypeId,
            @Parameter(example = "true")
            @RequestParam(required = false)
            boolean getActiveProduct) {
        return StandardResponses.success(productOfferingQuerryService
                .findByTelecomSubTypeOfferTypeCheckProductStatusMap(
                        telecomServiceId, subType, offerTypeId, getActiveProduct));
    }
}
