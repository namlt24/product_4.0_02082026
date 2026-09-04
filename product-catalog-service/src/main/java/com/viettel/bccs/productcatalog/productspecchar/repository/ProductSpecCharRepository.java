package com.viettel.bccs.productcatalog.productspecchar.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.productspecchar.entity.ProductSpecCharEntity;

@Repository
public interface ProductSpecCharRepository extends JpaRepository<ProductSpecCharEntity, Long>,
    ProductSpecCharRepositoryCustom {
    Optional<ProductSpecCharEntity> findByCode(String code);

    Optional<ProductSpecCharEntity> findByCodeAndStatus(String code, String status);

    boolean existsByCode(String code);
}