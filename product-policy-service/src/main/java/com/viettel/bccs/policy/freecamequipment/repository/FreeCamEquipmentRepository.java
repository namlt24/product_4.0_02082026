package com.viettel.bccs.policy.freecamequipment.repository;

import com.viettel.bccs.policy.freecamequipment.entity.FreeCamEquipmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FreeCamEquipmentRepository extends JpaRepository<FreeCamEquipmentEntity, Long>, FreeCamEquipmentRepositoryCustom {

}
