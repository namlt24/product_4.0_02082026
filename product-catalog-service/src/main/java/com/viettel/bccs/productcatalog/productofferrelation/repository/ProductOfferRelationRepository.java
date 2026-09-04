package com.viettel.bccs.productcatalog.productofferrelation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.productofferrelation.entity.ProductOfferRelationEntity;

@Repository
public interface ProductOfferRelationRepository extends JpaRepository<ProductOfferRelationEntity, Long> {

    List<ProductOfferRelationEntity> findByMainOfferId(Long mainOfferId);

    List<ProductOfferRelationEntity> findByRelationOfferId(Long relationOfferId);

    Optional<ProductOfferRelationEntity> findByMainOfferIdAndRelationOfferIdAndStatus(Long mainOfferId,
        Long relationOfferId, String status);

    List<ProductOfferRelationEntity> findByMainOfferIdAndStatus(Long mainOfferId, String status);

    List<ProductOfferRelationEntity> findByRelationTypeId(Long relationTypeId);

    boolean existsByMainOfferIdAndRelationOfferId(Long mainOfferId, Long relationOfferId);
}