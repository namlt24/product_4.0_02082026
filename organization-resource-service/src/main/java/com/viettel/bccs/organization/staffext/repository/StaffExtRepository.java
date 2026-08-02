package com.viettel.bccs.organization.staffext.repository;

import com.viettel.bccs.organization.staffext.entity.StaffExtEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StaffExtRepository extends JpaRepository<StaffExtEntity, Long>, StaffExtRepositoryCustom {

    List<StaffExtEntity> findByStaffId(Long staffId);

    List<StaffExtEntity> findByStaffIdAndStatus(Long staffId, String status);

    boolean existsByStaffExtId(Long staffExtId);
}