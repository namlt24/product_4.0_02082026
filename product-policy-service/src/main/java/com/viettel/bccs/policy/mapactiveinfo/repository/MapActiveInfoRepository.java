package com.viettel.bccs.policy.mapactiveinfo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.mapactiveinfo.entity.MapActiveInfoEntity;

@Repository
public interface MapActiveInfoRepository
        extends JpaRepository<MapActiveInfoEntity, Long>, MapActiveInfoRepositoryCustom {
}