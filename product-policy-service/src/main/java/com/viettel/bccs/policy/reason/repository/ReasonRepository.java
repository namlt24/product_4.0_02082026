package com.viettel.bccs.policy.reason.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.reason.entity.ReasonEntity;

@Repository
public interface ReasonRepository extends JpaRepository<ReasonEntity, Long>, ReasonRepositoryCustom {

}