package com.viettel.bccs.area.area.repository;

import com.viettel.bccs.area.area.entity.AreaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AreaRepository extends JpaRepository<AreaEntity, String> {

    Optional<AreaEntity> findByAreaCode(String areaCode);

    List<AreaEntity> findByParentCode(String parentCode);

    List<AreaEntity> findByProvince(String province);

    List<AreaEntity> findByStatus(String status);
}