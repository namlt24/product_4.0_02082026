package com.viettel.bccs.organization.staff.repository;

import com.viettel.bccs.organization.staff.entity.StaffEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StaffRepository extends JpaRepository<StaffEntity, Long> {

    Optional<StaffEntity> findByStaffIdAndStatus(Long staffId, String status);
    Optional<StaffEntity> findByStaffCodeAndStatus(String staffCode, String status);
}