package com.viettel.bccs.organization.staff.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.organization.staff.entity.StaffEntity;

@Repository
public interface StaffRepository extends JpaRepository<StaffEntity, Long> {

    Optional<StaffEntity> findByStaffIdAndStatus(Long staffId, String status);

    Optional<StaffEntity> findByStaffCodeAndStatus(String staffCode, String status);

    List<StaffEntity> findAllByShopIdAndStatus(Long shopId, String status);
}
