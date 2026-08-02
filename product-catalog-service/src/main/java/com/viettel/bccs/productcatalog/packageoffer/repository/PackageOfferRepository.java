package com.viettel.bccs.productcatalog.packageoffer.repository;

import com.viettel.bccs.productcatalog.packageoffer.entity.PackageOfferEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PackageOfferRepository extends JpaRepository<PackageOfferEntity, Long>, PackageOfferRepositoryCustom {
}