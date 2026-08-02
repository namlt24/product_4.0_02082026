package com.viettel.bccs.organization.identitytype.repository;

import com.viettel.bccs.organization.identitytype.entity.IdentityTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IdentityTypeRepository extends JpaRepository<IdentityTypeEntity, String> {

    List<IdentityTypeEntity> findAllByStatus(String status);
}