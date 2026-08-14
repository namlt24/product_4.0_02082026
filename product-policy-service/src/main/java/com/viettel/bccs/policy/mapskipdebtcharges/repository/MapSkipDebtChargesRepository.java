package com.viettel.bccs.policy.mapskipdebtcharges.repository;

import com.viettel.bccs.policy.mapskipdebtcharges.entity.MapSkipDebtChargesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapSkipDebtChargesRepository
        extends JpaRepository<MapSkipDebtChargesEntity, Long>, MapSkipDebtChargesRepositoryCustom {
}
