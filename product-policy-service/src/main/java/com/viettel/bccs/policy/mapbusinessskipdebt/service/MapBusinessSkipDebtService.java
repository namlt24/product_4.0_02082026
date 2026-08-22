package com.viettel.bccs.policy.mapbusinessskipdebt.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.request.SearchSkipDebtRequest;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.response.MapBusinessSkipDebtResponse;
import com.viettel.bccs.policy.mapbusinessskipdebt.dto.response.SkipDebtResultResponse;
import com.viettel.bccs.policy.mapbusinessskipdebt.entity.MapBusinessSkipDebtEntity;
import com.viettel.bccs.policy.mapbusinessskipdebt.mapper.MapBusinessSkipDebtMapper;
import com.viettel.bccs.policy.mapbusinessskipdebt.repository.MapBusinessSkipDebtRepository;
import com.viettel.bccs.policy.utils.RequestValidator;
import com.viettel.bccs.policy.utils.ValidationPatterns;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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
            throw new BusinessException("BCCS-POLICY-SKIPDEBT-0001", "Map business skip debt not found with id: " + mapId);
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


    public List<SkipDebtResultResponse> searchForAPI(SearchSkipDebtRequest request) {
        RequestValidator.requireNotBlank(request.actionCode(), "actionCode", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(request.actionCode(), "actionCode", 10, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(request.actionCode(), "actionCode", ValidationPatterns.ALPHANUMERIC, "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.requireNotNull(request.telecomServiceId(), "telecomServiceId", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkRange(request.telecomServiceId(), "telecomServiceId", 0L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
        RequestValidator.requireNotBlank(request.effectDatetime(), "effectDatetime", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkPattern(request.effectDatetime(), "effectDatetime", ValidationPatterns.DATE_DDMMYYYY, "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.requireNotBlank(request.shopCode(), "shopCode", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(request.shopCode(), "shopCode", 40, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(request.shopCode(), "shopCode", ValidationPatterns.CODE, "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.requireNotBlank(request.staffCode(), "staffCode", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(request.staffCode(), "staffCode", 40, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(request.staffCode(), "staffCode", ValidationPatterns.CODE, "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.requireNotBlank(request.businessNo(), "businessNo", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(request.businessNo(), "businessNo", 2000, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(request.businessNo(), "businessNo", ValidationPatterns.CODE, "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(request.contractNo(), "contractNo", 2000, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(request.contractNo(), "contractNo", ValidationPatterns.CODE, "BCCS-POLICY-VALIDATE-PATTERN");

        List<MapBusinessSkipDebtEntity> entities = repository.searchForAPI(
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