package com.viettel.bccs.policy.ref.refproductpackage.repository;

import com.viettel.bccs.policy.ref.refproductpackage.entity.RefProductPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefProductPackageRepository extends JpaRepository<RefProductPackageEntity, Long> {


}