package com.viettel.bccs.policy.mapping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.mapping.entity.MappingEntity;

@Repository
public interface MappingRepository extends JpaRepository<MappingEntity, Long>, MappingRepositoryCustom {

    List<MappingEntity> findByReasonIdAndActionCodeAndTelServiceIdAndStatus(Long reasonId, String actionCode,
            Long telServiceId, String status);
}