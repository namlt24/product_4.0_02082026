package com.viettel.bccs.productcatalog.prodpackproductoffertype.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.prodpackproductoffertype.entity.ProdPackProductOfferTypeEntity;

@Repository
public interface ProdPackProductOfferTypeRepository extends JpaRepository<ProdPackProductOfferTypeEntity, Long>,
    ProdPackProductOfferTypeRepositoryCustom {

}