package com.viettel.bccs.productcatalog.telecomservice.repository;

import com.viettel.bccs.productcatalog.telecomservice.entity.TelecomServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TelecomServiceRepository extends JpaRepository<TelecomServiceEntity, Long>, TelecomServiceRepositoryCustom {
}
