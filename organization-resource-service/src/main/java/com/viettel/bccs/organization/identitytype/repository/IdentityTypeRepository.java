package com.viettel.bccs.organization.identitytype.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.organization.identitytype.entity.IdentityTypeEntity;

@Repository
public interface IdentityTypeRepository extends JpaRepository<IdentityTypeEntity, String> {

    List<IdentityTypeEntity> findAllByStatus(String status);

    Optional<IdentityTypeEntity> findByIdTypeAndStatus(String idType, String status);
}
