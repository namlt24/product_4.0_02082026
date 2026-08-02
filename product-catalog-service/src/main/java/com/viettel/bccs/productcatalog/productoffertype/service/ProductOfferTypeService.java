package com.viettel.bccs.productcatalog.productoffertype.service;

import com.viettel.bccs.productcatalog.productoffertype.dto.response.ProductOfferTypeDTO;
import com.viettel.bccs.productcatalog.productoffertype.entity.ProductOfferTypeEntity;
import com.viettel.bccs.productcatalog.productoffertype.mapper.ProductOfferTypeMapper;
import com.viettel.bccs.productcatalog.productoffertype.repository.ProductOfferTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOfferTypeService {

    private final ProductOfferTypeRepository repository;
    private final ProductOfferTypeMapper mapper;

//    @Cacheable(value = "productOfferTypeCache", key = "'BY_IDS:' + T(String).join(',', #ids.stream().sorted().toList())")
    public Map<Long, ProductOfferTypeDTO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return repository.findByIds(ids).stream()
                .map(mapper::toDto)
                .collect(Collectors.toMap(ProductOfferTypeDTO::getProductOfferTypeId, Function.identity()));
    }
}