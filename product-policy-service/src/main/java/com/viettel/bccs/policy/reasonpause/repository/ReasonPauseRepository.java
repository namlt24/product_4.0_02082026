package com.viettel.bccs.policy.reasonpause.repository;

import com.viettel.bccs.policy.reasonpause.entity.ReasonPauseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReasonPauseRepository extends JpaRepository<ReasonPauseEntity, Long> {

    List<ReasonPauseEntity> findByReasonIdAndStatus(Long reasonId, String status);

    List<ReasonPauseEntity> findByReasonIdInAndStatus(List<Long> reasonIds, String status);
}
