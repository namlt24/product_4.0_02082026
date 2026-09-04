package com.viettel.bccs.organization.staffext.repository;

import java.util.Optional;

import com.viettel.bccs.organization.staffext.entity.StaffExtEntity;

public interface StaffExtRepositoryCustom {

    Optional<StaffExtEntity> findByStaffIdAndKey(Long staffId, String key, String checkValue);
}
