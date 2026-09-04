package com.viettel.bccs.policy.freecamequipment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.viettel.bccs.policy.freecamequipment.entity.FreeCamEquipmentEntity;

@Repository
public interface FreeCamEquipmentRepository extends JpaRepository<FreeCamEquipmentEntity, Long>,
        FreeCamEquipmentRepositoryCustom {

}
