package com.viettel.bccs.policy.ref.refprodpackpotype.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.ref.refprodpackpotype.dto.RefProdPackPoTypeDTO;
import com.viettel.bccs.policy.ref.refprodpackpotype.service.RefProdPackPoTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product-policy-service/v1/ref-prod-pack-po-type")
@RequiredArgsConstructor
public class RefProdPackPoTypeController {

    private final RefProdPackPoTypeService refProdPackPoTypeService;

    @GetMapping("/findAllActive")
    @Operation(operationId = "API_POLICY_REF_001",
            summary = "Lấy danh sách REF_PROD_PACK_PO_TYPE đang hiệu lực",
            description = "API lấy tất cả REF_PROD_PACK_PO_TYPE có trạng thái hiệu lực")
    public StandardResponse<List<RefProdPackPoTypeDTO>> findAllActive() {
        return StandardResponses.success(refProdPackPoTypeService.findAllActive());
    }

    @GetMapping("/findByProductPackageId/{productPackageId}")
    @Operation(operationId = "API_POLICY_REF_002",
            summary = "Lấy REF_PROD_PACK_PO_TYPE theo ID gói sản phẩm",
            description = "API lấy danh sách theo productPackageId")
    public StandardResponse<List<RefProdPackPoTypeDTO>> findByProductPackageId(
            @Parameter(description = "ID gói sản phẩm", example = "10")
            @PathVariable Long productPackageId) {
        return StandardResponses.success(refProdPackPoTypeService.findByProductPackageId(productPackageId));
    }

    @GetMapping("/findByProductOfferTypeId/{productOfferTypeId}")
    @Operation(operationId = "API_POLICY_REF_003",
            summary = "Lấy REF_PROD_PACK_PO_TYPE theo ID loại sản phẩm",
            description = "API lấy danh sách theo productOfferTypeId")
    public StandardResponse<List<RefProdPackPoTypeDTO>> findByProductOfferTypeId(
            @Parameter(description = "ID loại sản phẩm", example = "5")
            @PathVariable Long productOfferTypeId) {
        return StandardResponses.success(refProdPackPoTypeService.findByProductOfferTypeId(productOfferTypeId));
    }
}