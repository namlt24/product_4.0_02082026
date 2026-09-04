package com.viettel.bccs.policy.reasoncharuse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.reasoncharuse.entity.ReasonCharUseEntity;

@Repository
public interface ReasonCharUseRepository extends JpaRepository<ReasonCharUseEntity, Long> {

    List<ReasonCharUseEntity> findByReasonIdInAndStatus(List<Long> reasonIds, String status);
}
