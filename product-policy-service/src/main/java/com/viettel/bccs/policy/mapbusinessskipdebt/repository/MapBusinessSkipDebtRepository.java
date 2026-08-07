package com.viettel.bccs.policy.mapbusinessskipdebt.repository;

import com.viettel.bccs.policy.mapbusinessskipdebt.entity.MapBusinessSkipDebtEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapBusinessSkipDebtRepository
        extends JpaRepository<MapBusinessSkipDebtEntity, Long>, MapBusinessSkipDebtRepositoryCustom {
}