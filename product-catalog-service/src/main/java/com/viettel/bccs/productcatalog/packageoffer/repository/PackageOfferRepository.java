package com.viettel.bccs.productcatalog.packageoffer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.packageoffer.entity.PackageOfferEntity;

@Repository
public interface PackageOfferRepository extends JpaRepository<PackageOfferEntity, Long>, PackageOfferRepositoryCustom {
}