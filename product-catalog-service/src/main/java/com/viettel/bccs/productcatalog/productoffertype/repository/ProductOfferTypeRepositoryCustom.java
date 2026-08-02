package com.viettel.bccs.productcatalog.productoffertype.repository;

import com.viettel.bccs.productcatalog.productoffertype.entity.ProductOfferTypeEntity;

import java.util.List;

public interface ProductOfferTypeRepositoryCustom {

    List<ProductOfferTypeEntity> findByIds(List<Long> ids);
}