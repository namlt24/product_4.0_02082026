package com.viettel.bccs.productcatalog.product.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.common.dto.FilterRequest;
import com.viettel.bccs.productcatalog.product.dto.request.CheckProductAttByRuleTypeRequest;
import com.viettel.bccs.productcatalog.product.dto.request.FindProductOfferingByListCodeListSpecCodeRequest;
import com.viettel.bccs.productcatalog.product.dto.request.GetListStockTypeWSRequest;
import com.viettel.bccs.productcatalog.product.dto.response.CheckProductAttByRuleTypeResponse;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferTypeStockDTO;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingCharacterFullDTO;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingDTO;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingResponse;
import com.viettel.bccs.productcatalog.product.dto.response.SubTypeDTO;
import com.viettel.bccs.productcatalog.product.openapi.ApiCheckAttProductOrVasByCode;
import com.viettel.bccs.productcatalog.product.openapi.ApiCheckProductAttByRuleType;
import com.viettel.bccs.productcatalog.product.openapi.ApiFindByCodeOrId;
import com.viettel.bccs.productcatalog.product.openapi.ApiFindByCodesAndProductOfferType;
import com.viettel.bccs.productcatalog.product.openapi.ApiFindByIds;
import com.viettel.bccs.productcatalog.product.openapi.ApiFindByPayTypeWithSpec;
import com.viettel.bccs.productcatalog.product.openapi.ApiFindByTelecomSubTypeOfferType;
import com.viettel.bccs.productcatalog.product.openapi.ApiFindByTelecomSubTypeOfferTypeCheckProductStatus;
import com.viettel.bccs.productcatalog.product.openapi.ApiFindProductOfferingByListCodeListSpecCode;
import com.viettel.bccs.productcatalog.product.openapi.ApiGetByProductCode;
import com.viettel.bccs.productcatalog.product.openapi.ApiGetListOfferAlterStatus;
import com.viettel.bccs.productcatalog.product.openapi.ApiGetListPricePlanByOfferId;
import com.viettel.bccs.productcatalog.product.openapi.ApiGetListProductOfferingBySpecChars;
import com.viettel.bccs.productcatalog.product.openapi.ApiGetListStockTypeWS;
import com.viettel.bccs.productcatalog.product.openapi.ApiGetListVas;
import com.viettel.bccs.productcatalog.product.openapi.ApiGetSubTypeByProductCode;
import com.viettel.bccs.productcatalog.product.openapi.ApiHasProductAtt;
import com.viettel.bccs.productcatalog.product.service.ProductOfferingService;
import com.viettel.bccs.productcatalog.product.service.StockTypeWSService;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.service.ProductOfferCharUseService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-catalog-service/v1/product")
@RequiredArgsConstructor
@Tag(name = "Product Offering")
public class ProductOfferingController {

    private final ProductOfferingService productOfferingService;
    private final ProductOfferCharUseService productOfferCharUseService;
    private final StockTypeWSService stockTypeWSService;

    @ApiGetByProductCode
    @GetMapping("/getByProductCode")
    public StandardResponse<ProductOfferingResponse> getByProductCode(
            @Parameter(example = "PACKAGE_001", required = true)
            @RequestParam(required = false)
            String productCode) {
        return StandardResponses.success(productOfferingService.getByProductCode(productCode));
    }

    @ApiGetListOfferAlterStatus
    @GetMapping("/getListOfferAlterStatus")
    public StandardResponse<List<ProductOfferingDTO>> getListOfferAlterStatus(
            @Parameter(example = "500001", required = true)
            @RequestParam(required = false)
            Long offerId,

            @Parameter(example = "ONLINE", required = true)
            @RequestParam(required = false)
            String changeChannel,

            @Parameter(example = "true")
            @RequestParam boolean checkStatus) {
        return StandardResponses.success(productOfferingService.getListOfferAlterStatus(offerId, changeChannel, checkStatus));
    }

    @GetMapping("/findByTelecomSubTypeOfferTypeCheckProductStatus")
    @ApiFindByTelecomSubTypeOfferTypeCheckProductStatus
    public StandardResponse<List<ProductOfferingDTO>> findByTelecomSubTypeOfferTypeCheckProductStatus(
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
            @RequestParam boolean getActiveProduct) {
        return StandardResponses.success(productOfferingService.findByTelecomSubTypeOfferTypeCheckProductStatus(telecomServiceId, subType, offerTypeId, getActiveProduct));
    }

    @GetMapping("/findByTelecomSubTypeOfferType")
    @ApiFindByTelecomSubTypeOfferType
    public StandardResponse<List<ProductOfferingDTO>> findByTelecomSubTypeOfferType(
            @Parameter(example = "1")
            @RequestParam(required = false)
            Long telecomServiceId,
            @Parameter(example = "1")
            @RequestParam(required = false)
            String subType,
            @Parameter(example = "1")
            @RequestParam(required = false)
            Long offerTypeId) {
        return StandardResponses.success(productOfferingService.findByTelecomSubTypeOfferType(telecomServiceId, subType, offerTypeId));
    }

    @GetMapping("/findByCodeOrId")
    @ApiFindByCodeOrId
    public StandardResponse<List<ProductOfferingDTO>> findByCodeOrId(
            @Parameter(example = "12345")
            @RequestParam(required = false)
            Long proOfferId,
            @Parameter(example = "PACKAGE_001")
            @RequestParam(required = false)
            String prodOfferCode,
            @Parameter(example = "1")
            @RequestParam(required = false)
            String status) {
        return StandardResponses.success(productOfferingService.findByCodeOrId(proOfferId, prodOfferCode, status));
    }

    @PostMapping("/findByPayTypeWithSpec")
    @ApiFindByPayTypeWithSpec
    public StandardResponse<List<ProductOfferingDTO>> findByPayTypeWithSpec(
            @Parameter(example = "1")
            @RequestParam(required = false)
            String telecomServiceId,

            @Parameter(example = "1", required = true)
            @RequestParam(required = false)
            String payType,

            @Parameter(example = "1", required = true)
            @RequestParam(required = false)
            String productOfferTypeId,

            @Parameter
            @RequestBody(required = false)
            List<FilterRequest> listProductSpec) {
        return StandardResponses.success(productOfferingService.findByPayTypeWithSpec(telecomServiceId, payType, productOfferTypeId, listProductSpec));
    }

    @GetMapping("/checkAttProductOrVasByCode")
    @ApiCheckAttProductOrVasByCode
    public StandardResponse<Boolean> checkAttProductOrVasByCode(
            @Parameter(example = "300", required = true)
            @RequestParam(required = false)
            String productCode,

            @Parameter(example = "1", required = true)
            @RequestParam(required = false)
            String productType,

            @Parameter(example = "IS_CONNECTED", required = true)
            @RequestParam(required = false)
            String attributeCode) {
        return StandardResponses.success(productOfferingService.checkAttProductOrVasByCode(productCode, productType, attributeCode));
    }

    @GetMapping("/hasProductAtt")
    @ApiHasProductAtt
    public StandardResponse<Boolean> hasProductAtt(
            @Parameter(example = "400005827", required = true)
            @RequestParam(required = false)
            Long offerId,

            @Parameter(example = "IS_CONNECTED", required = true)
            @RequestParam(required = false)
            String attributeCode) {
        return StandardResponses.success(productOfferingService.hasProductAtt(offerId, attributeCode));
    }

    @PostMapping("/getListProductOfferingBySpecChars")
    @ApiGetListProductOfferingBySpecChars
    public StandardResponse<List<ProductOfferingDTO>> getListProductOfferingBySpecChars(
            @Parameter(example = "[\"IS_CONNECTED\", \"DATA_CAP\"]")
            @RequestBody
            List<String> specCodes,

            @Parameter(example = "1")
            @RequestParam(required = false)
            Long productOfferTypeId) {
        return StandardResponses.success(productOfferingService.getListProductOfferingBySpecChars(specCodes, productOfferTypeId));
    }

    @PostMapping("/checkProductAttByRuleType")
    @ApiCheckProductAttByRuleType
    public StandardResponse<CheckProductAttByRuleTypeResponse> checkProductAttByRuleType(
            @Parameter(required = true)
            @RequestBody
            CheckProductAttByRuleTypeRequest request) {
        return StandardResponses.success(productOfferingService.checkProductAttByRuleType(request));
    }

    @PostMapping("/findByCodesAndProductOfferType")
    @ApiFindByCodesAndProductOfferType
    public StandardResponse<List<ProductOfferingDTO>> findByCodesAndProductOfferType(
            @Parameter(example = "[\"CODE_001\", \"CODE_002\"]")
            @RequestBody
            List<String> codes,
            @Parameter(example = "1", required = true)
            @RequestParam(required = false)
            Long productOfferTypeId) {
        return StandardResponses.success(productOfferingService.findByCodesAndProductOfferType(codes, productOfferTypeId));
    }

    @PostMapping("/findByIds")
    @ApiFindByIds
    public StandardResponse<List<ProductOfferingDTO>> findByIds(
            @Parameter(example = "[12345, 67890]")
            @RequestBody
            List<Long> offerIds) {
        return StandardResponses.success(productOfferingService.findByIds(offerIds));
    }

    @GetMapping("/getListPricePlanByOfferId")
    @ApiGetListPricePlanByOfferId
    public StandardResponse<List<ProductOfferingCharacterFullDTO>> getListPricePlanByOfferId(
            @Parameter(example = "500001", required = true)
            @RequestParam(required = false)
            Long productOfferingId) {
        return StandardResponses.success(productOfferCharUseService.getListPricePlanByOfferId(productOfferingId));
    }

    @GetMapping("/getListVas")
    @ApiGetListVas
    public StandardResponse<List<ProductOfferingDTO>> getListVas(
            @Parameter(example = "500001", required = true)
            @RequestParam(required = false)
            Long offerId,

            @Parameter(example = "1")
            @RequestParam(required = false)
            Integer type) {
        return StandardResponses.success(productOfferingService.getListVas(offerId, type));
    }

    @PostMapping("/getListStockTypeWS")
    @ApiGetListStockTypeWS
    public StandardResponse<List<ProductOfferTypeStockDTO>> getListStockTypeWS(@RequestBody GetListStockTypeWSRequest request) {
        return StandardResponses.success(stockTypeWSService.getListStockTypeWS(request));
    }

    @GetMapping("/getSubTypeByProductCode")
    @ApiGetSubTypeByProductCode
    public StandardResponse<SubTypeDTO> getSubTypeByProductCode(
            @Parameter(example = "PACKAGE_001", required = true)
            @RequestParam
            String productCode) {
        return StandardResponses.success(productOfferingService.getSubTypeByProductCode(productCode));
    }

    @PostMapping("/findProductOfferingByListCodeListSpecCode")
    @ApiFindProductOfferingByListCodeListSpecCode
    public StandardResponse<List<ProductOfferingDTO>> findProductOfferingByListCodeListSpecCode(
            @RequestBody FindProductOfferingByListCodeListSpecCodeRequest request) {
        return StandardResponses.success(productOfferingService.findProductOfferingByListCodeListSpecCode(
                request.getLstProductOfferCode(), request.getLstSpecCode(), request.getProductOfferType()));
    }
}
