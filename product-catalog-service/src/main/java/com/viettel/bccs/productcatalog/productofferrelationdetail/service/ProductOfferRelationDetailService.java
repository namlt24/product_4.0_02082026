package com.viettel.bccs.productcatalog.productofferrelationdetail.service;

import org.springframework.stereotype.Service;

import com.viettel.bccs.productcatalog.productofferrelationdetail.mapper.ProductOfferRelationDetailMapper;
import com.viettel.bccs.productcatalog.productofferrelationdetail.repository.ProductOfferRelationDetailRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductOfferRelationDetailService {

    private final ProductOfferRelationDetailRepository productOfferRelationDetailRepository;
    private final ProductOfferRelationDetailMapper productOfferRelationDetailMapper;

}