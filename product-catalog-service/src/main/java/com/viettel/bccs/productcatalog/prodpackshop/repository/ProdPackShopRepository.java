package com.viettel.bccs.productcatalog.prodpackshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.prodpackshop.entity.ProdPackShopEntity;

@Repository
public interface ProdPackShopRepository extends JpaRepository<ProdPackShopEntity, Long>, ProdPackShopRepositoryCustom {

}