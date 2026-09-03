package com.viettel.bccs.organization.stockchannelmapping.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.organization.shop.dto.ShopDTO;
import com.viettel.bccs.organization.staff.dto.StaffDTO;
import com.viettel.bccs.organization.stockchannelmapping.dto.response.StockChannelMappingResponse;
import com.viettel.bccs.organization.stockchannelmapping.entity.StockChannelMappingEntity;
import com.viettel.bccs.organization.stockchannelmapping.mapper.StockChannelMappingMapper;
import com.viettel.bccs.organization.stockchannelmapping.repository.StockChannelMappingRepository;
import com.viettel.bccs.organization.utils.Const;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockChannelMappingService {

    private final StockChannelMappingRepository mappingRepository;
    private final StockChannelMappingMapper mappingMapper;

    @Transactional(readOnly = true)
    public List<StockChannelMappingResponse> findActive() {
        log.info("Truy vấn tất cả mapping kho - kênh đang hiệu lực");
        return mappingRepository.findAllByStatus(Const.STATUS.ACTIVE)
                .stream()
                .map(mappingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockChannelMappingResponse> findByChannelType(Long channelTypeId) {
        log.info("Truy vấn mapping theo loại kênh: {}", channelTypeId);
        return mappingRepository.findAllByChannelTypeIdAndStatus(channelTypeId, Const.STATUS.ACTIVE)
                .stream()
                .map(mappingMapper::toResponse)
                .toList();
    }

}
