package com.viettel.bccs.policy.ref.refproductpackagefee.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.ref.refproductpackagefee.entity.RefProductPackageFeeEntity;

@Repository
public interface RefProductPackageFeeRepository extends JpaRepository<RefProductPackageFeeEntity, Long> {

}