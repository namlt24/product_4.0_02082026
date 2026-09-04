package com.viettel.bccs.policy.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.viettel.bccs.policy.client.dto.StandardClientResponse;

@FeignClient(
        name = ProductCatalogFeignClient.CLIENT_NAME,
        url = "${spring.cloud.openfeign.client.config.product-catalog-service.url}"
)
public interface ProductCatalogFeignClient {

    String CLIENT_NAME = "product-catalog-service";

    @GetMapping("/v1/optionsetvalue/findByOptionSetCode/{code}")
    ResponseEntity<StandardClientResponse> findValueByOptionSetCode(@PathVariable String code);

    @GetMapping("/v1/optionsetvalue/findByOptionSetCodes")
    ResponseEntity<StandardClientResponse> findByOptionSetCodes(@RequestParam List<String> codes);

    @GetMapping("/v1/optionsetvalue/getValueByTwoCodeOption")
    ResponseEntity<StandardClientResponse> getValueByTwoCodeOption(@RequestParam String optSetCode,
            @RequestParam String name);

    @PostMapping("/v1/product-offer-char-use/getProductSpecCharByOfferingIds")
    ResponseEntity<StandardClientResponse> getProductSpecCharByOfferingIds(@RequestBody List<String> offeringIds);

    @GetMapping("/v1/product-package/getPackageCodesByProductOfferTypeCount")
    ResponseEntity<StandardClientResponse> getPackageCodesByProductOfferTypeCount(
            @RequestParam String excludeProdOfferType, @RequestParam(required = false) Integer packageNumber);

    @PostMapping("/v1/productspecchar/findByIds")
    ResponseEntity<StandardClientResponse> findSpecCharByIds(@RequestBody List<Long> ids);

    @PostMapping("/v1/productspectcharvalue/findByIds")
    ResponseEntity<StandardClientResponse> findSpecCharValueByIds(@RequestBody List<Long> ids);

    @GetMapping("/v1/product/getListOfferAlterStatus")
    ResponseEntity<StandardClientResponse> getListOfferAlterStatus(
            @RequestParam Long offerId, @RequestParam String changeChannel, @RequestParam boolean checkStatus);

    @GetMapping("/v1/product/findByTelecomSubTypeOfferTypeCheckProductStatus")
    ResponseEntity<StandardClientResponse> findByTelecomSubTypeOfferTypeCheckProductStatus(
            @RequestParam(required = false) Long telecomServiceId,
            @RequestParam(required = false) String subType,
            @RequestParam(required = false) Long offerTypeId,
            @RequestParam boolean getActiveProduct);

    @PostMapping("/v1/product/findByCodesAndProductOfferType")
    ResponseEntity<StandardClientResponse> findByCodesAndProductOfferType(
            @RequestBody List<String> codes, @RequestParam Long productOfferTypeId);

    @PostMapping("/v1/product/findByIds")
    ResponseEntity<StandardClientResponse> findByIds(@RequestBody List<Long> offerIds);

    @GetMapping("/v1/telecom-service/getTelServiceByAlias")
    ResponseEntity<StandardClientResponse> getTelServiceByAlias(@RequestParam String alias);
}
