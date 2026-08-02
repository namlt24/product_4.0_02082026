package com.viettel.bccs.productcatalog.productspeccharvalue.repository;

import com.viettel.bccs.productcatalog.productspeccharvalue.entity.ProductSpecCharValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductSpecCharValueRepository extends JpaRepository<ProductSpecCharValueEntity, Long> {

    List<ProductSpecCharValueEntity> findByProductSpecCharId(Long productSpecCharId);

    Optional<ProductSpecCharValueEntity> findByProductSpecCharIdAndIsDefault(Long productSpecCharId, Long isDefault);

    List<ProductSpecCharValueEntity> findByProductSpecCharIdAndStatus(Long productSpecCharId, String status);

    boolean existsByProductSpecCharId(Long productSpecCharId);
}