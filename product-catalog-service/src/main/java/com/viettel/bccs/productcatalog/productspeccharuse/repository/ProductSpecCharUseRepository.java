package com.viettel.bccs.productcatalog.productspeccharuse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.productspeccharuse.entity.ProductSpecCharUseEntity;

@Repository
public interface ProductSpecCharUseRepository extends JpaRepository<ProductSpecCharUseEntity, Long> {

    List<ProductSpecCharUseEntity> findByProductSpecCharId(Long productSpecCharId);

    List<ProductSpecCharUseEntity> findByProductSpecIdAndStatus(Long productSpecId, String status);

    Optional<ProductSpecCharUseEntity> findByProductSpecIdAndProductSpecCharId(Long productSpecId,
        Long productSpecCharId);
}
