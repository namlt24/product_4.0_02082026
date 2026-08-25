package com.viettel.bccs.policy.mapping.service;

import com.viettel.bccs.policy.mapping.dto.response.MappingResponse;
import com.viettel.bccs.policy.mapping.mapper.MappingMapper;
import com.viettel.bccs.policy.mapping.repository.MappingRepository;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
import com.viettel.bccs.policy.reason.mapper.ReasonMapper;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.DataUtil;
import com.viettel.bccs.policy.utils.RequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MappingService {

    private final MappingRepository repository;
    private final MappingMapper mapper;
    private final ReasonMapper reasonMapper;

    public List<String> findSaleServiceCodeByReason(Long reasonId) {
        return repository.findSaleServiceCodeByReason(reasonId);
    }

    public Map<String, String> getLstMapPackageByActionCodeAndReasonCodes(List<String> reasonCodes, String actionCode) {
        return repository.getLstMapPackageByActionCodeAndReasonCodes(reasonCodes, actionCode);
    }


    public List<ReasonResponse> getMappingReasonProductOfferPrice(Long productPackageId) {
        return repository.getMappingReasonProductOfferPrice(productPackageId)
                .stream()
                .map(reasonMapper::toResponse)
                .toList();
    }

    public List<MappingResponse> getMappingByMultiParams(Long reasonId, String actionCode, Long telServiceId) {
        return repository.findByReasonIdAndActionCodeAndTelServiceIdAndStatus(reasonId, actionCode, telServiceId, Const.STATUS.ACTIVE)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    /**
     * Migrate từ mono: ExternalServiceForMbccs.getListStockTypeWS bước tìm saleServiceCode.
     * Nếu reasonId null hoặc 0 thì trả về null ngay, không query (giữ đúng hành vi legacy).
     * Phục vụ product-catalog-service gọi cross-service (qua MappingClient) khi dựng API
     * getListStockTypeWS.
     */
    public String getSaleServiceCode(Long telecomServiceId, Long reasonId, String productCode, String actionCode) {
        RequestValidator.requireNotNull(reasonId, "reasonId", "BCCS-PRODUCT-VALIDATE-0000");
        if (reasonId == null || reasonId == 0L) {
            return null;
        }
        return repository.getSaleServiceCode(telecomServiceId, reasonId, productCode, actionCode);
    }
}