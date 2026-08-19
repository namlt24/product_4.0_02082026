package com.viettel.bccs.organization.shop.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.organization.channeltype.service.ChannelTypeService;
import com.viettel.bccs.organization.shop.dto.ShopDTO;
import com.viettel.bccs.organization.shop.dto.response.StockCodeResponse;
import com.viettel.bccs.organization.shop.mapper.ShopMapper;
import com.viettel.bccs.organization.shop.repository.ShopRepository;
import com.viettel.bccs.organization.staff.dto.StaffDTO;
import com.viettel.bccs.organization.staff.service.StaffService;
import com.viettel.bccs.organization.utils.Const;
import com.viettel.bccs.organization.utils.DataUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final ShopMapper shopMapper;
    private final StaffService staffService;
    private final ChannelTypeService channelTypeService;


    @Cacheable(value = "shopCache", key = "'SHOP:' + #shopId")
    @Transactional(readOnly = true)
    public ShopDTO getActiveById(Long shopId) {
        log.info("Truy vấn cửa hàng active từ DB theo id: {}", shopId);
        return shopRepository.findByShopIdAndStatus(shopId, Const.STATUS.ACTIVE)
                .map(shopMapper::toResponse)
                .orElseThrow(() -> new BusinessException("BCCS-ORGANIZATION-SHOP-0001", "Không tìm thấy cửa hàng với id: " + shopId));
    }

    @Cacheable(value = "shopCache", key = "'SHOP:' + #shopCode")
    @Transactional(readOnly = true)
    public ShopDTO getActiveByShopCode(String shopCode) {
        log.info("Truy vấn cửa hàng active từ DB theo mã: {}", shopCode);
        return shopRepository.findByShopCodeAndStatus(shopCode, Const.STATUS.ACTIVE)
                .map(shopMapper::toResponse)
                .orElseThrow(() -> new BusinessException("BCCS-ORGANIZATION-SHOP-0002", "Không tìm thấy cửa hàng với mã: " + shopCode));
    }

    @Transactional(readOnly = true)
    public ShopDTO getActiveByIdWithChannelOfAgent(Long shopId) {
        log.info("Truy vấn cửa hàng active theo id kèm cờ isChannelOfAgent: {}", shopId);
        ShopDTO dto = getActiveById(shopId);
        dto.setIsChannelOfAgent(channelTypeService.isChannelOfAgent(dto.getChannelTypeId()));
        return dto;
    }


    @Transactional(readOnly = true)
    public StockCodeResponse getStockCode(Long ownerId, Integer ownerType) {
        if (ownerType == Const.ShopService.OWNER_TYPE_SHOP) {
            ShopDTO shop = getActiveById(ownerId);
            return new StockCodeResponse(shop.getShopCode());
        }
        if (ownerType == Const.ShopService.OWNER_TYPE_STAFF) {
            StaffDTO staffDTO = staffService.getActiveById(ownerId);
            return new StockCodeResponse(staffDTO.getStaffCode());
        }
        throw new BusinessException("BCCS-ORGANIZATION-SHOP-0003", "Loại owner không hợp lệ: " + ownerType);
    }

    @Cacheable(value = "shopCache", key = "'ACTIVE_BATCH:' + #shopIds.stream().sorted().toList().toString()")
    @Transactional(readOnly = true)
    @Operation(
            summary = "Tìm danh sách cửa hàng active theo nhiều shopId",
            description = "Truy vấn danh sách cửa hàng có status = 1 theo danh sách shopId. " +
                    "Query được chia batch (100 bản ghi/batch) để tránh lỗi ORA-01795 khi danh sách lớn. " +
                    "Kết quả được cache để tăng hiệu suất khi gọi lại với cùng danh sách shopId."
    )
    public List<ShopDTO> findActiveByShopIds(
            @Parameter(description = "Danh sách ID cửa hàng cần truy vấn", required = true) List<Long> shopIds) {
        if (DataUtil.isNullOrEmpty(shopIds)) {
            return new ArrayList<>();
        }
        return shopRepository.findActiveByShopIds(shopIds).stream()
                .map(shopMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ShopDTO> findActiveByChannelType(Long channelTypeId) {
        log.info("Truy vấn danh sách cửa hàng active theo loại kênh: {}", channelTypeId);
        return shopRepository.findAllByChannelTypeIdAndStatus(channelTypeId, Const.STATUS.ACTIVE)
                .stream()
                .map(shopMapper::toResponse)
                .toList();
    }
}