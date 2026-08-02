package com.viettel.bccs.policy.ref.refproductpackagefee.repository;

import com.viettel.bccs.policy.ref.refproductpackagefee.entity.RefProductPackageFeeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefProductPackageFeeRepository extends JpaRepository<RefProductPackageFeeEntity, Long> {

}