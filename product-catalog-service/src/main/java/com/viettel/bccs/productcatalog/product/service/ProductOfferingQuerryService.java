package com.viettel.bccs.productcatalog.product.service;

import java.util.HashMap;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.productcatalog.product.repository.ProductOfferingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOfferingQuerryService {

    private final ProductOfferingRepository productOfferingRepository;

    public HashMap<Long, String> findByTelecomSubTypeOfferTypeCheckProductStatusMap(
            Long telecomServiceId, String subType, Long offerTypeId, boolean getActiveProduct) {
        HashMap<Long, String> result = new HashMap<>();
        productOfferingRepository.findByTelecomSubTypeOfferTypeCheckProductStatus(telecomServiceId, subType,
            offerTypeId, getActiveProduct)                .forEach(entity -> result.put(entity.getProductOfferingId(),
                entity.getSubType()));
        return result;
    }
}