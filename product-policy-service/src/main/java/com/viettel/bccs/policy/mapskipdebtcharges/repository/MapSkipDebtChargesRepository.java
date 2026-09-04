package com.viettel.bccs.policy.mapskipdebtcharges.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.mapskipdebtcharges.entity.MapSkipDebtChargesEntity;

@Repository
public interface MapSkipDebtChargesRepository
        extends JpaRepository<MapSkipDebtChargesEntity, Long>, MapSkipDebtChargesRepositoryCustom {
}
