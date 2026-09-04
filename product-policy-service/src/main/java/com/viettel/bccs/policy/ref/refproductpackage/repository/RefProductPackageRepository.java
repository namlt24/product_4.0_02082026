package com.viettel.bccs.policy.ref.refproductpackage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.ref.refproductpackage.entity.RefProductPackageEntity;

@Repository
public interface RefProductPackageRepository extends JpaRepository<RefProductPackageEntity, Long> {


}