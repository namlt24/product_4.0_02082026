package com.viettel.bccs.organization.staffext.repository;

import com.viettel.bccs.organization.staffext.entity.StaffExtEntity;

import java.util.Optional;

public interface StaffExtRepositoryCustom {

    Optional<StaffExtEntity> findByStaffIdAndKey(Long staffId, String key, String checkValue);
}