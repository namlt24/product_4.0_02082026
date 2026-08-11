package com.viettel.bccs.productcatalog.client;

import com.viettel.bccs.productcatalog.client.dto.StandardClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = OrganizationResourceFeignClient.CLIENT_NAME,
        url = "${spring.cloud.openfeign.client.config.organization-resource-service.url}"
)
public interface OrganizationResourceFeignClient {

    String CLIENT_NAME = "organization-resource-service";
    String FIND_ACTIVE_BY_CUST_TYPE_PATH = "/v1/cust-type/findActiveByCustType/{custType}";
    String CHECK_REASON_SENSOR_FEE_PATH = "/v1/sensor-fee/checkReasonSensorFee/{productPackageId}";
    String FIND_ACTIVE_BY_SHOP_IDS_PATH = "/v1/shop/findActiveByShopIds";

    @GetMapping(FIND_ACTIVE_BY_CUST_TYPE_PATH)
    ResponseEntity<StandardClientResponse> findActiveByCustType(@PathVariable String custType);

    @GetMapping(CHECK_REASON_SENSOR_FEE_PATH)
    ResponseEntity<StandardClientResponse> checkReasonSensorFee(@PathVariable Long productPackageId);

    @PostMapping(FIND_ACTIVE_BY_SHOP_IDS_PATH)
    ResponseEntity<StandardClientResponse> findActiveByShopIds(@RequestBody List<Long> shopIds);

    @GetMapping("/v1/staff/getStaffShopFullInfo/{staffCode}")
    ResponseEntity<StandardClientResponse> getStaffShopFullInfo(@PathVariable String staffCode);
}