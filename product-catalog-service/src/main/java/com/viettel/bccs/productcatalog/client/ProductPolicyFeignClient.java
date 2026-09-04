package com.viettel.bccs.productcatalog.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.viettel.bccs.productcatalog.client.dto.StandardClientResponse;

@FeignClient(
        name = ProductPolicyFeignClient.CLIENT_NAME,
        url = "${spring.cloud.openfeign.client.config.product-policy-service.url}"
)
public interface ProductPolicyFeignClient {

    String CLIENT_NAME = "product-policy-service";
    String FIND_SALE_SERVICE_CODE_BY_REASON_PATH = "/v1/mapping/findSaleServiceCodeByReason/{reasonId}";
    String GET_MAPPING_REASON_PRODUCT_OFFER_PRICE_PATH =
            "/v1/mapping/getMappingReasonProductOfferPrice/{productPackageId}";

    @GetMapping(FIND_SALE_SERVICE_CODE_BY_REASON_PATH)
    ResponseEntity<StandardClientResponse> findSaleServiceCodeByReason(@PathVariable Long reasonId);

    @GetMapping(GET_MAPPING_REASON_PRODUCT_OFFER_PRICE_PATH)
    ResponseEntity<StandardClientResponse> getMappingReasonProductOfferPrice(@PathVariable Long productPackageId);

    @GetMapping("/v1/free-cam-equipment/checkReasonFreeCam/{productPackageId}")
    ResponseEntity<StandardClientResponse> checkReasonFreeCam(@PathVariable Long productPackageId);

    @GetMapping("/v1/reason/getReasonIdByTypeAndCode")
    ResponseEntity<StandardClientResponse> getReasonIdByTypeAndCode(@RequestParam String reasonCode,
                                                                      @RequestParam String actionCode,
                                                                      @RequestParam Long telecomServiceId);

    @GetMapping("/v1/mapping/getSaleServiceCode")
    ResponseEntity<StandardClientResponse> getSaleServiceCode(@RequestParam(required = false) Long telecomServiceId,
                                                                @RequestParam Long reasonId,
                                                                @RequestParam(required = false) String productCode,
                                                                @RequestParam(required = false) String actionCode);
}