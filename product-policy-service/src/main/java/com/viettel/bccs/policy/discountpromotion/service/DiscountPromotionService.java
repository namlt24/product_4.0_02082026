package com.viettel.bccs.policy.discountpromotion.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionDTO;
import com.viettel.bccs.policy.discountpromotion.dto.response.DiscountPromotionResponse;
import com.viettel.bccs.policy.discountpromotion.entity.DiscountPromotionEntity;
import com.viettel.bccs.policy.discountpromotion.mapper.DiscountPromotionMapper;
import com.viettel.bccs.policy.discountpromotion.repository.DiscountPromotionRepository;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoQuerryService;
import com.viettel.bccs.policy.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiscountPromotionService {

    private final DiscountPromotionRepository repository;
    private final DiscountPromotionMapper mapper;

    private MapActiveInfoQuerryService mapActiveInfoQuerryService;

    public DiscountPromotionResponse findById(Long id) {
        Optional<DiscountPromotionEntity> entity = repository.findById(id);
        if (entity.isEmpty()) {
            throw new BusinessException("BCCS-POLICY-001", "Discount promotion not found with id: " + id);
        }
        return mapper.toResponse(entity.get());
    }

    public List<DiscountPromotionDTO> getPromotionList(
            Long telecomServiceId,
            boolean checkStatus,
            boolean checkEffectDate,
            LocalDateTime endDate) {
        List<DiscountPromotionEntity> entities = repository.getPromotionList(
                telecomServiceId, checkStatus, checkEffectDate, endDate);
        return mapper.toDTOList(entities);
    }

    public List<DiscountPromotionDTO> getPromFromMapActiveInfos(List<MapActiveInfoDTO> mapActiveInfos, int mode, boolean getDuplicateProm) {
        if (DataUtil.isNullOrEmpty(mapActiveInfos)) {
            return new ArrayList<>();
        }
        List<MapActiveInfoDTO> tMapActiveInfos = mapActiveInfoQuerryService.getMapActiveInfosByLevel(mapActiveInfos, "promCode", mode);
        return repository.getPromFromMapActiveInfosCheckDuplicate(tMapActiveInfos, getDuplicateProm);
    }
}