package com.viettel.bccs.policy.ref.refprodpackpotype.repository;

import com.viettel.bccs.policy.ref.refprodpackpotype.entity.RefProdPackPoTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RefProdPackPoTypeRepository extends JpaRepository<RefProdPackPoTypeEntity, Long> {

    List<RefProdPackPoTypeEntity> findAllByStatus(String status);

    List<RefProdPackPoTypeEntity> findAllByProductPackageIdAndStatus(Long productPackageId, String status);

    List<RefProdPackPoTypeEntity> findAllByProductOfferTypeIdAndStatus(Long productOfferTypeId, String status);
}