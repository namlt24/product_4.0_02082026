package com.viettel.bccs.productcatalog.packageoffer.service;

import com.viettel.bccs.productcatalog.productpackage.dto.response.PackageOfferDTO;
import com.viettel.bccs.productcatalog.packageoffer.entity.PackageOfferEntity;
import com.viettel.bccs.productcatalog.packageoffer.mapper.PackageOfferMapper;
import com.viettel.bccs.productcatalog.packageoffer.repository.PackageOfferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PackageOfferService {

    private final PackageOfferRepository repository;
    private final PackageOfferMapper mapper;

//    @Cacheable(value = "packageOfferCache", key = "'BY_PROD_PACK_TYPE_IDS:' + T(String).join(',', #prodPackTypeIds.stream().sorted().toList())")
    public Map<Long, List<PackageOfferDTO>> getPackageOfferByListProdPackTypeIds(List<Long> prodPackTypeIds) {
        if (prodPackTypeIds == null || prodPackTypeIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, List<PackageOfferEntity>> entityMap = repository.getPackageOfferByListProdPackTypeIds(prodPackTypeIds);
        return entityMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().map(mapper::toDto).toList()
                ));
    }
}