package com.viettel.bccs.productcatalog.prodpackshop.repository;

import com.viettel.bccs.productcatalog.prodpackshop.entity.ProdPackShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdPackShopRepository extends JpaRepository<ProdPackShopEntity, Long>, ProdPackShopRepositoryCustom {

}