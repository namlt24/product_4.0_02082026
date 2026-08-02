package com.viettel.bccs.productcatalog.productofferrelation.repository;

import com.viettel.bccs.productcatalog.productofferrelation.entity.ProductOfferRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOfferRelationRepository extends JpaRepository<ProductOfferRelationEntity, Long> {

    List<ProductOfferRelationEntity> findByMainOfferId(Long mainOfferId);

    List<ProductOfferRelationEntity> findByRelationOfferId(Long relationOfferId);

    Optional<ProductOfferRelationEntity> findByMainOfferIdAndRelationOfferIdAndStatus(Long mainOfferId, Long relationOfferId, String status);

    List<ProductOfferRelationEntity> findByMainOfferIdAndStatus(Long mainOfferId, String status);

    List<ProductOfferRelationEntity> findByRelationTypeId(Long relationTypeId);

    boolean existsByMainOfferIdAndRelationOfferId(Long mainOfferId, Long relationOfferId);
}