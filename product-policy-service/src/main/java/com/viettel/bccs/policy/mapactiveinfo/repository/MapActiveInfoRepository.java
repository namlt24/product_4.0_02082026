package com.viettel.bccs.policy.mapactiveinfo.repository;

import com.viettel.bccs.policy.mapactiveinfo.entity.MapActiveInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapActiveInfoRepository
        extends JpaRepository<MapActiveInfoEntity, Long>, MapActiveInfoRepositoryCustom {
}