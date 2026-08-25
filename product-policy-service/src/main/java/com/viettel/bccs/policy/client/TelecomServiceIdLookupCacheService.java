package com.viettel.bccs.policy.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.bccs.common.error.exception.IntegrationException;
import com.viettel.bccs.policy.client.dto.TelecomServiceDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Cache boundary riêng cho {@link TelecomServiceClientImpl#getServiceIdByAlias} — tách khỏi
 * {@code TelecomServiceClientImpl} vì lý do KHÁC với các cache boundary khác trong dự án (không
 * phải self-invocation): giá trị gốc là {@code Long} — khi qua Redis L2 (chế độ
 * {@code bccs.cache.mode=two-level}), Jackson deserialize số nguyên KHÔNG kèm type metadata thành
 * {@code Integer}, khiến Spring cache interceptor ném {@code ClassCastException} khi ép lại về
 * {@code Long} (đã tái hiện thật: request đầu tiên sau khi app start, L1 Caffeine rỗng, đọc lại
 * L2 Redis thì crash). Chuyển giá trị cache sang {@code String} (kiểu đã chứng minh round-trip
 * đúng qua Redis ở {@link OptionSetClientImpl#getValueByTwoCodeOption}) để tránh nhập nhằng kiểu
 * số — {@link TelecomServiceClientImpl#getServiceIdByAlias} parse lại thành {@code Long} ở boundary.
 */
@Service
@Slf4j
@RequiredArgsConstructor
class TelecomServiceIdLookupCacheService {

    private final ProductCatalogFeignClient productCatalogFeignClient;
    private final ObjectMapper objectMapper;

    @Cacheable(value = "telecomServiceClientCache", key = "#alias")
    public String getServiceIdByAliasCached(String alias) {
        try {
            var response = productCatalogFeignClient.getTelServiceByAlias(alias).getBody();
            if (response != null && response.getData() != null) {
                TelecomServiceDTO dto = objectMapper.convertValue(response.getData(), TelecomServiceDTO.class);
                return dto != null && dto.getTelecomServiceId() != null ? dto.getTelecomServiceId().toString() : null;
            }
            return null;
        } catch (RuntimeException e) {
            log.error("Error calling getTelServiceByAlias: alias={}", alias, e);
            throw new IntegrationException("BCCS-SYS-PTC-0001",
                    "Error calling product-catalog-service getTelServiceByAlias for alias=" + alias, e);
        }
    }
}
