package com.viettel.bccs.productcatalog.productofferrelation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.productcatalog.productofferrelation.dto.response.ProductOfferRelationResponse;
import com.viettel.bccs.productcatalog.productofferrelation.mapper.ProductOfferRelationMapper;
import com.viettel.bccs.productcatalog.productofferrelation.repository.ProductOfferRelationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductOfferRelationService {

    private final ProductOfferRelationRepository productOfferRelationRepository;
    private final ProductOfferRelationMapper productOfferRelationMapper;

    /**
     * Map 1:1 tu productOfferRelationService.findByMainOfferId(offerId) cua code mono cu
     * (dung trong ProductOfferingServiceImpl.getListVasCore) - lay TOAN BO quan he cua 1 offer
     * chinh, khong loc theo relationTypeId (viec loc theo relationTypeId=VAS duoc lam sau,
     * o tang goi, giong het code cu).
     */
    public List<ProductOfferRelationResponse> findByMainOfferId(Long mainOfferId) {
        return productOfferRelationRepository.findByMainOfferId(mainOfferId).stream()
                .map(productOfferRelationMapper::toResponse)
                .toList();
    }

}
