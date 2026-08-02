package com.viettel.bccs.productcatalog.prodpackproductoffertype.repository;

import com.viettel.bccs.productcatalog.prodpackproductoffertype.entity.ProdPackProductOfferTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdPackProductOfferTypeRepository extends JpaRepository<ProdPackProductOfferTypeEntity, Long>, ProdPackProductOfferTypeRepositoryCustom {

}