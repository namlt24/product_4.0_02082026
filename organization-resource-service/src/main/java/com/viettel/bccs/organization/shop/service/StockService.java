package com.viettel.bccs.organization.shop.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.organization.shop.dto.ShopDTO;
import com.viettel.bccs.organization.staff.dto.StaffDTO;
import com.viettel.bccs.organization.staff.dto.StockDTO;
import com.viettel.bccs.organization.staff.service.StaffService;
import com.viettel.bccs.organization.stockchannelmapping.repository.StockChannelMappingRepository;
import com.viettel.bccs.organization.utils.Const;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StaffService staffService;
    private final ShopService shopService;
    private final StockChannelMappingRepository stockChannelMappingRepository;

    @Transactional(readOnly = true)
    public List<StockDTO> getListStockMbccs(String staffCode, Long telServiceId) {
        if (staffCode == null || staffCode.isBlank()) {
            throw new BusinessException("BCCS-ORGANIZATION-STAFF-0004",
                    "Mã user không tồn tại hoặc đang không ở trạng thái hoạt động. Vui lòng kiểm tra lại");
        }
        staffCode = staffCode.trim();
        StaffDTO staffDTO;
        try {
            staffDTO = staffService.findActiveByStaffCode(staffCode);
        } catch (BusinessException e) {
            throw new BusinessException("BCCS-ORGANIZATION-STAFF-0004",
                    "Mã user " + staffCode + " không tồn tại hoặc đang không ở trạng thái hoạt động. Vui lòng kiểm tra lại");
        }

        ShopDTO shopDTO = null;
        if (staffDTO.getShopId() != null) {
            try {
                shopDTO = shopService.getActiveById(staffDTO.getShopId());
            } catch (BusinessException e) {
                log.warn("Không tìm thấy cửa hàng active của user, bỏ qua kho đơn vị: {}", staffDTO.getShopId());
            }
        }

        List<StockDTO> functionalStocks = resolveFunctionalStocks(staffDTO, shopDTO, telServiceId);

        List<StockDTO> resultList = new ArrayList<>();
        // Kho cá nhân (type 2) = mã user
        resultList.add(new StockDTO(staffDTO.getStaffId(), staffDTO.getStaffCode(), staffDTO.getName(), "2"));
        // Kho đơn vị (type 1) = mã shop của user
        if (shopDTO != null) {
            resultList.add(new StockDTO(shopDTO.getShopId(), shopDTO.getShopCode(), shopDTO.getName(), "1"));
        }
        // Kho chức năng (type 3) từ mapping
        resultList.addAll(functionalStocks);
        return resultList;
    }

    @Transactional(readOnly = true)
    public boolean validateStockMapping(String staffCode, String stockCode, Long telServiceId) {
        StaffDTO staffDTO;
        try {
            staffDTO = staffService.findActiveByStaffCode(staffCode);
        } catch (BusinessException e) {
            throw new BusinessException("BCCS-ORGANIZATION-STAFF-0004",
                    "Mã user " + staffCode + " không tồn tại hoặc đang không ở trạng thái hoạt động. Vui lòng kiểm tra lại");
        }

        boolean isPersonalStock = stockCode.equals(staffDTO.getStaffCode());
        if (!isPersonalStock) {
            try {
                shopService.getActiveByShopCode(stockCode);
            } catch (BusinessException e) {
                throw new BusinessException("BCCS-ORGANIZATION-STOCK-0001",
                        "Kho số " + stockCode + " không tồn tại. Vui lòng kiểm tra lại");
            }
        }

        ShopDTO shopDTO = null;
        if (staffDTO.getShopId() != null) {
            try {
                shopDTO = shopService.getActiveById(staffDTO.getShopId());
            } catch (BusinessException e) {
                log.warn("Không tìm thấy cửa hàng active của user khi validate mapping: {}", staffDTO.getShopId());
            }
        }

        Set<String> validCodes = new HashSet<>();
        validCodes.add(staffDTO.getStaffCode());
        if (shopDTO != null) {
            validCodes.add(shopDTO.getShopCode());
        }
        if (telServiceId != null && shopDTO != null && shopDTO.getChannelTypeId() != null && staffDTO.getShopId() != null) {
            List<Long> grantedStockIds = stockChannelMappingRepository.findActiveFunctionalStockIds(
                    telServiceId, shopDTO.getChannelTypeId(), staffDTO.getShopId(),
                    staffDTO.getStaffId(), new Date(), Const.STATUS.ACTIVE);
            toFunctionalStockDTOs(grantedStockIds).forEach(s -> validCodes.add(s.getCode()));
        }

        if (!validCodes.contains(stockCode)) {
            throw new BusinessException("BCCS-ORGANIZATION-STOCK-0002",
                    "Kho số " + stockCode + " không được mapping với user " + staffCode + ". Vui lòng kiểm tra lại");
        }
        return true;
    }

    private List<StockDTO> resolveFunctionalStocks(StaffDTO staffDTO, ShopDTO shopDTO, Long telServiceId) {
        List<Long> grantedStockIds = new ArrayList<>();
        if (telServiceId != null && shopDTO != null && shopDTO.getChannelTypeId() != null && staffDTO.getShopId() != null) {
            grantedStockIds = stockChannelMappingRepository.findActiveFunctionalStockIds(
                    telServiceId, shopDTO.getChannelTypeId(), staffDTO.getShopId(),
                    staffDTO.getStaffId(), new Date(), Const.STATUS.ACTIVE);
        }
        return toFunctionalStockDTOs(grantedStockIds);
    }

    private List<StockDTO> toFunctionalStockDTOs(List<Long> grantedStockIds) {
        if (grantedStockIds == null || grantedStockIds.isEmpty()) {
            return new ArrayList<>();
        }
        boolean grantAllStocks = grantedStockIds.contains(-1L);
        List<ShopDTO> stocks;
        if (grantAllStocks) {
            stocks = shopService.findActiveByChannelType(Const.CHANNEL_TYPE.STOCK_FUNCTIONAL_CHANNEL);
        } else {
            stocks = shopService.findActiveByShopIds(grantedStockIds);
        }
        return stocks.stream()
                .map(shop -> new StockDTO(shop.getShopId(), shop.getShopCode(), shop.getName(), "3"))
                .toList();
    }

}
