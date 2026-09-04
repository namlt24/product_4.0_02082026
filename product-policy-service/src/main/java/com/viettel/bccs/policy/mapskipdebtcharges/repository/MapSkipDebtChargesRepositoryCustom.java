package com.viettel.bccs.policy.mapskipdebtcharges.repository;

import java.util.List;

import com.viettel.bccs.policy.mapskipdebtcharges.dto.response.MapSkipDebtChargesDTO;
import com.viettel.bccs.policy.mapskipdebtcharges.entity.MapSkipDebtChargesEntity;

public interface MapSkipDebtChargesRepositoryCustom {

    /**
     * Tìm MAP_SKIP_DEBT_CHARGES theo mẫu (example): mỗi field khác null/rỗng trên {@code example}
     * trở thành 1 điều kiện AND, field null/rỗng bị bỏ qua (không lọc theo field đó).
     */
    List<MapSkipDebtChargesEntity> findByExample(MapSkipDebtChargesDTO example) throws Exception;
}
