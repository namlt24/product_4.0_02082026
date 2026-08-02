package com.viettel.bccs.productcatalog.productspecchar.repository;

import com.viettel.bccs.productcatalog.productspecchar.entity.ProductSpecCharEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductSpecCharRepository extends JpaRepository<ProductSpecCharEntity, Long> {

    Optional<ProductSpecCharEntity> findByCode(String code);

    Optional<ProductSpecCharEntity> findByCodeAndStatus(String code, String status);

    boolean existsByCode(String code);
}