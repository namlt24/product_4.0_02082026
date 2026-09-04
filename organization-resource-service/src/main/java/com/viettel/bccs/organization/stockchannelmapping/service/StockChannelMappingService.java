package com.viettel.bccs.organization.stockchannelmapping.service;

import java.util.Date;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.organization.stockchannelmapping.dto.response.StockChannelMappingResponse;
import com.viettel.bccs.organization.stockchannelmapping.mapper.StockChannelMappingMapper;
import com.viettel.bccs.organization.stockchannelmapping.repository.StockChannelMappingRepository;
import com.viettel.bccs.organization.utils.Const;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockChannelMappingService {

    private final StockChannelMappingRepository mappingRepository;
    private final StockChannelMappingMapper mappingMapper;

    @Transactional(readOnly = true)
    public List<StockChannelMappingResponse> findActive() {
        log.info("Truy vấn tất cả mapping kho - kênh đang hiệu lực");
        return mappingRepository.findAllByStatus(Const.Status.ACTIVE)
                .stream()
                .map(mappingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StockChannelMappingResponse> findByChannelType(Long channelTypeId) {
        log.info("Truy vấn mapping theo loại kênh: {}", channelTypeId);
        return mappingRepository.findAllByChannelTypeIdAndStatus(channelTypeId, Const.Status.ACTIVE)
                .stream()
                .map(mappingMapper::toResponse)
                .toList();
    }

    // Cache key phai co ngay hom nay: query loc theo EFFECT_DATE/EXPIRE_DATE so voi "today", neu
    // khong co ngay trong key thi 1 mapping het han/moi hieu luc luc nua dem van bi cache tra ve
    // ket qua cua ngay truoc do trong het TTL (toi 1h).
    @Cacheable(value = "stockChannelMappingCache",
            key = "'FUNC_STOCKS:' + #telServiceId + ':' + #channelTypeId + ':' + #shopId + ':' + #staffId"
                    + " + ':' + T(java.time.LocalDate).now()")
    @Transactional(readOnly = true)
    public List<Long> findActiveFunctionalStockIds(Long telServiceId, Long channelTypeId, Long shopId, Long staffId) {
        log.info("Truy vấn kho số chức năng đang hiệu lực từ DB:"
                + " telServiceId={}, channelTypeId={}, shopId={}, staffId={}",
                telServiceId, channelTypeId, shopId, staffId);
        return mappingRepository.findActiveFunctionalStockIds(
                telServiceId, channelTypeId, shopId, staffId, new Date(), Const.Status.ACTIVE);
    }

}
