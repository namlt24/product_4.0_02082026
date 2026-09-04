package com.viettel.bccs.productcatalog.telecomservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.productcatalog.telecomservice.entity.TelecomServiceEntity;

@Repository
public interface TelecomServiceRepository extends JpaRepository<TelecomServiceEntity, Long>,
    TelecomServiceRepositoryCustom {
    }
