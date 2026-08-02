package com.viettel.bccs.policy.mapping.repository;

import java.util.List;

public interface MappingRepositoryCustom {

    List<String> findSaleServiceCodeByReason(Long reasonId);
}