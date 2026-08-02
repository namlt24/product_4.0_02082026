package com.viettel.bccs.productcatalog.productofferrelation.service;

import com.viettel.bccs.productcatalog.productofferrelation.mapper.ProductOfferRelationMapper;
import com.viettel.bccs.productcatalog.productofferrelation.repository.ProductOfferRelationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductOfferRelationService {

    private final ProductOfferRelationRepository productOfferRelationRepository;
    private final ProductOfferRelationMapper productOfferRelationMapper;

}