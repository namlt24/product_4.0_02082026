package com.viettel.bccs.policy.freecamequipment.repository;

import java.util.List;

import com.viettel.bccs.policy.freecamequipment.entity.FreeCamEquipmentEntity;

public interface FreeCamEquipmentRepositoryCustom {

    /**
     * Migrate từ mono: FreeCamEquipmentRepoImpl.checkReasonFreeCam(Long productPackageId).
     * SQL gốc:
     * Select * from free_cam_equipment
     * Where status = 1
     * And effect_datetime <= trunc(sysdate)
     * And (expire_datetime >= trunc(sysdate) or expire_datetime is null)
     * And reason_id in
     *   (Select c.reason_id From mapping a, reason c
     *    Where a.reason_id = c.reason_id And a.status = 1 and c.status = 1
     *    And (a.end_effect_date is null or a.end_effect_date >= trunc(sysdate))
     *    And a.sale_service_id = :productPackageId)
     */
    List<FreeCamEquipmentEntity> checkReasonFreeCam(Long productPackageId);
}
