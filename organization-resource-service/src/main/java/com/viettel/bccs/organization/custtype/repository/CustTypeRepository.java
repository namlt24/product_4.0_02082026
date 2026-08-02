package com.viettel.bccs.organization.custtype.repository;

import com.viettel.bccs.organization.custtype.entity.CustTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustTypeRepository extends JpaRepository<CustTypeEntity, String>, CustTypeRepositoryCustom {

    Optional<CustTypeEntity> findByCustTypeAndStatus(String custType, String status);

    List<CustTypeEntity> findAllByStatus(String status);

    boolean existsByCustType(String custType);
}