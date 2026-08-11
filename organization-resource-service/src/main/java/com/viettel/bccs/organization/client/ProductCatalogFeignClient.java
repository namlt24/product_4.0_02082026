package com.viettel.bccs.organization.client;

import com.viettel.bccs.organization.client.dto.StandardClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = ProductCatalogFeignClient.CLIENT_NAME,
        url = "${spring.cloud.openfeign.client.config.product-catalog-service.url}"
)
public interface ProductCatalogFeignClient {

    String CLIENT_NAME = "product-catalog-service";
    String FIND_BY_OPTION_SET_CODE_PATH = "/v1/optionsetvalue/findByOptionSetCode/{code}";

    @GetMapping(FIND_BY_OPTION_SET_CODE_PATH)
    ResponseEntity<StandardClientResponse> findValueByOptionSetCode(@PathVariable String code);
}
