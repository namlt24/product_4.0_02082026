package com.viettel.bccs.policy.mapbusinessskipdebt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.mapbusinessskipdebt.entity.MapBusinessSkipDebtEntity;

@Repository
public interface MapBusinessSkipDebtRepository
        extends JpaRepository<MapBusinessSkipDebtEntity, Long>, MapBusinessSkipDebtRepositoryCustom {
}