package com.viettel.bccs.policy.mapbusinessskipdebt.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.request.SearchSkipDebtRequest;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.response.MapBusinessSkipDebtResponse;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.response.SkipDebtResultResponse;
import com.viettel.bccs.policy.mapbusinessskipdebt.entity.MapBusinessSkipDebtEntity;
import com.viettel.bccs.policy.mapbusinessskipdebt.mapper.MapBusinessSkipDebtMapper;
import com.viettel.bccs.policy.mapbusinessskipdebt.repository.MapBusinessSkipDebtRepository;
import com.viettel.bccs.policy.utils.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapBusinessSkipDebtService {

    private final MapBusinessSkipDebtRepository repository;
    private final MapBusinessSkipDebtMapper mapper;

    public MapBusinessSkipDebtResponse findById(Long mapId) {
        Optional<MapBusinessSkipDebtEntity> entity = repository.findById(mapId);
        if (entity.isEmpty()) {
            throw new BusinessException("BCCS-POLICY-SKIPDEBT-0001", "Map business skip debt not found with id: " +
                    mapId);
        }
        return mapper.toResponse(entity.get());
    }

    public List<MapBusinessSkipDebtResponse> findActiveByActionCodeAndTelecomServiceId(
            String actionCode, Long telecomServiceId) {
        List<MapBusinessSkipDebtEntity> entities = repository
                .findActiveByActionCodeAndTelecomServiceId(actionCode, telecomServiceId);
        return mapper.toResponseList(entities);
    }

    public List<MapBusinessSkipDebtResponse> findActiveByShopId(Long shopId) {
        List<MapBusinessSkipDebtEntity> entities = repository.findActiveByShopId(shopId);
        return mapper.toResponseList(entities);
    }

    public List<MapBusinessSkipDebtResponse> findActiveByStaffId(Long staffId) {
        List<MapBusinessSkipDebtEntity> entities = repository.findActiveByStaffId(staffId);
        return mapper.toResponseList(entities);
    }

    public List<MapBusinessSkipDebtResponse> findAll() {
        return mapper.toResponseList(repository.findAll());
    }


    public List<SkipDebtResultResponse> searchForApi(SearchSkipDebtRequest request) {
        RequestValidator.requireNotBlank(request.actionCode(), "actionCode", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotNull(request.telecomServiceId(), "telecomServiceId", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(request.effectDatetime(), "effectDatetime", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(request.shopCode(), "shopCode", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(request.staffCode(), "staffCode", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(request.businessNo(), "businessNo", "BCCS-PRODUCT-VALIDATE-0000");

        List<MapBusinessSkipDebtEntity> entities = repository.searchForApi(
                request.actionCode(),
                request.telecomServiceId(),
                request.effectDatetime(),
                request.shopCode(),
                request.staffCode(),
                request.businessNo(),
                request.contractNo()
        );
        return mapper.toSearchResultList(entities);
    }
}