package com.viettel.bccs.policy.reasonpause.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.reasonpause.entity.ReasonPauseEntity;

@Repository
public interface ReasonPauseRepository extends JpaRepository<ReasonPauseEntity, Long> {

    List<ReasonPauseEntity> findByReasonIdAndStatus(Long reasonId, String status);

    List<ReasonPauseEntity> findByReasonIdInAndStatus(List<Long> reasonIds, String status);
}
