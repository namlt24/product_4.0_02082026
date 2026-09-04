package com.viettel.bccs.policy.ref.refprodpackpotype.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.ref.refprodpackpotype.dto.RefProdPackPoTypeDTO;
import com.viettel.bccs.policy.ref.refprodpackpotype.openapi.ApiFindAllActive;
import com.viettel.bccs.policy.ref.refprodpackpotype.openapi.ApiFindByProductOfferTypeId;
import com.viettel.bccs.policy.ref.refprodpackpotype.openapi.ApiFindByProductPackageId;
import com.viettel.bccs.policy.ref.refprodpackpotype.service.RefProdPackPoTypeService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-policy-service/v1/ref-prod-pack-po-type")
@RequiredArgsConstructor
public class RefProdPackPoTypeController {

    private final RefProdPackPoTypeService refProdPackPoTypeService;

    @GetMapping("/findAllActive")
    @ApiFindAllActive
    public StandardResponse<List<RefProdPackPoTypeDTO>> findAllActive() {
        return StandardResponses.success(refProdPackPoTypeService.findAllActive());
    }

    @GetMapping("/findByProductPackageId/{productPackageId}")
    @ApiFindByProductPackageId
    public StandardResponse<List<RefProdPackPoTypeDTO>> findByProductPackageId(
            @Parameter(description = "ID gói sản phẩm", example = "10")
            @PathVariable
            Long productPackageId) {
        return StandardResponses.success(refProdPackPoTypeService.findByProductPackageId(productPackageId));
    }

    @GetMapping("/findByProductOfferTypeId/{productOfferTypeId}")
    @ApiFindByProductOfferTypeId
    public StandardResponse<List<RefProdPackPoTypeDTO>> findByProductOfferTypeId(
            @Parameter(description = "ID loại sản phẩm", example = "5")
            @PathVariable
            Long productOfferTypeId) {
        return StandardResponses.success(refProdPackPoTypeService.findByProductOfferTypeId(productOfferTypeId));
    }
}
