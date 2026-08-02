package com.viettel.bccs.policy.mapping.repository;

import com.viettel.bccs.policy.mapping.entity.MappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MappingRepository extends JpaRepository<MappingEntity, Long>, MappingRepositoryCustom {

}