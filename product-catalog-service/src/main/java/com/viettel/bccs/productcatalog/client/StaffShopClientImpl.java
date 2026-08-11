package com.viettel.bccs.productcatalog.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.client.core.BccsHttpClient;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.productcatalog.client.dto.ShopDTO;
import com.viettel.bccs.productcatalog.client.dto.StaffShopResponse;
import com.viettel.bccs.productcatalog.client.dto.StandardClientResponse;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffShopClientImpl implements StaffShopClient {

    private final BccsHttpClient bccsHttpClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<ShopDTO> findActiveByShopIds(List<Long> shopIds) {
        if (DataUtil.isNullOrEmpty(shopIds)) {
            return Collections.emptyList();
        }
        try {
            var response = bccsHttpClient.post(
                    "organization-resource-service",
                    "/v1/shop/findActiveByShopIds",
                    shopIds,
                    StandardClientResponse.class);
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), new TypeReference<List<ShopDTO>>() {});
            }
            return Collections.emptyList();
        } catch (RuntimeException e) {
            log.error("Error calling findActiveByShopIds for {} shopIds", shopIds.size(), e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling organization-resource-service findActiveByShopIds for " + shopIds.size() + " shopIds", e);
        }
    }

    @Override
    @Cacheable(value = "staffShopClientCache", key = "#staffCode")
    public StaffShopResponse getStaffShopFullInfo(String staffCode) {
        try {
            var response = bccsHttpClient.get(
                    "organization-resource-service",
                    "/v1/staff/getStaffShopFullInfo/{staffCode}",
                    StandardClientResponse.class,
                    staffCode);
            if (response != null && response.getData() != null) {
                return objectMapper.convertValue(response.getData(), StaffShopResponse.class);
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getStaffShopFullInfo for staffCode: {}", staffCode, e);
            throw new IntegrationException("BCCS-SYS-INT-0001",
                    "Error calling organization-resource-service getStaffShopFullInfo for staffCode=" + staffCode, e);
        }
    }
}
