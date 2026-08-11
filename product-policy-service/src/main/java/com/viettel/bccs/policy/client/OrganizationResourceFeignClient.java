package com.viettel.bccs.policy.client;

import com.viettel.bccs.policy.client.dto.StandardClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = OrganizationResourceFeignClient.CLIENT_NAME,
        url = "${spring.cloud.openfeign.client.config.organization-resource-service.url}"
)
public interface OrganizationResourceFeignClient {

    String CLIENT_NAME = "organization-resource-service";

    @GetMapping("/v1/staff/getStaffShopFullInfo/{staffCode}")
    ResponseEntity<StandardClientResponse> getStaffShopFullInfo(@PathVariable String staffCode);

    @GetMapping("/v1/staffext/getStaffExtByStaffIDAndKey")
    ResponseEntity<StandardClientResponse> getStaffExtByStaffIDAndKey(
            @RequestParam Long staffId, @RequestParam String key);
}
