package com.viettel.bccs.productcatalog.productofferrelationdetail.repository;

import com.viettel.bccs.productcatalog.productofferrelationdetail.entity.ProductOfferRelationDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOfferRelationDetailRepository extends JpaRepository<ProductOfferRelationDetailEntity, Long> {

    List<ProductOfferRelationDetailEntity> findByProductOfferRelationId(Long productOfferRelationId);

    List<ProductOfferRelationDetailEntity> findByProductSpecCharId(Long productSpecCharId);

    Optional<ProductOfferRelationDetailEntity> findByProductOfferRelationIdAndProductSpecCharId(Long productOfferRelationId, Long productSpecCharId);

    List<ProductOfferRelationDetailEntity> findByProductOfferRelationIdAndStatus(Long productOfferRelationId, String status);

    boolean existsByProductOfferRelationId(Long productOfferRelationId);
}