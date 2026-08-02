package com.viettel.bccs.productcatalog.productofferrelationdetail.service;

import com.viettel.bccs.productcatalog.productofferrelationdetail.mapper.ProductOfferRelationDetailMapper;
import com.viettel.bccs.productcatalog.productofferrelationdetail.repository.ProductOfferRelationDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductOfferRelationDetailService {

    private final ProductOfferRelationDetailRepository productOfferRelationDetailRepository;
    private final ProductOfferRelationDetailMapper productOfferRelationDetailMapper;

}