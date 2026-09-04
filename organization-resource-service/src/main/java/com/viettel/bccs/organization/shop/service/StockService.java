package com.viettel.bccs.organization.shop.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.organization.shop.dto.ShopDTO;
import com.viettel.bccs.organization.staff.dto.StaffDTO;
import com.viettel.bccs.organization.staff.dto.StockDTO;
import com.viettel.bccs.organization.staff.service.StaffService;
import com.viettel.bccs.organization.stockchannelmapping.service.StockChannelMappingService;
import com.viettel.bccs.organization.utils.Const;
import com.viettel.bccs.organization.utils.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private static final long ALL_STOCKS = -1L;

    private final StaffService staffService;
    private final ShopService shopService;
    private final StockChannelMappingService stockChannelMappingService;

    @Cacheable(value = "getListStockMbccs", key = "'STOCKS_MBCCS:' + #staffCode + ':' + #telServiceId")
    @Transactional(readOnly = true)
    public List<StockDTO> getListStockMbccs(String staffCode, Long telServiceId) {
        RequestValidator.requireNotBlank(staffCode, "staffCode", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotNull(telServiceId, "telServiceId", "BCCS-PRODUCT-VALIDATE-0000");

        staffCode = staffCode.trim();
        StaffDTO staffDTO;
        try {
            staffDTO = staffService.findActiveByStaffCode(staffCode);
        } catch (BusinessException e) {
            throw new BusinessException("BCCS-ORGANIZATION-STAFF-0004",
                    "Mã user " + staffCode
                            + " không tồn tại hoặc đang không ở trạng thái hoạt động. Vui lòng kiểm tra lại");

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
        RequestValidator.requireNotBlank(staffCode, "staffCode", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(stockCode, "stockCode", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotNull(telServiceId, "telServiceId", "BCCS-PRODUCT-VALIDATE-0000");

        StaffDTO staffDTO;
        try {
            staffDTO = staffService.findActiveByStaffCode(staffCode);
        } catch (BusinessException e) {
            throw new BusinessException("BCCS-ORGANIZATION-STAFF-0004",
                    "Mã user " + staffCode
                            + " không tồn tại hoặc đang không ở trạng thái hoạt động. Vui lòng kiểm tra lại");

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
        if (telServiceId != null && shopDTO != null && shopDTO.getChannelTypeId() != null
                && staffDTO.getShopId() != null) {
            List<Long> grantedStockIds = stockChannelMappingService.findActiveFunctionalStockIds(
                    telServiceId, shopDTO.getChannelTypeId(), staffDTO.getShopId(), staffDTO.getStaffId());
            toFunctionalStockDtoList(grantedStockIds).forEach(s -> validCodes.add(s.getCode()));
        }

        if (!validCodes.contains(stockCode)) {
            throw new BusinessException("BCCS-ORGANIZATION-STOCK-0002",
                    "Kho số " + stockCode + " không được mapping với user " + staffCode + ". Vui lòng kiểm tra lại");
        }
        return true;
    }


    @Transactional(readOnly = true)
    public List<StockDTO> getListStockValid(String staffCode, List<Long> shopIds, Long telServiceId) {
        RequestValidator.requireNotBlank(staffCode, "staffCode", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotEmpty(shopIds, "shopIds", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotNull(telServiceId, "telServiceId", "BCCS-PRODUCT-VALIDATE-0000");

        Set<Long> remaining = new HashSet<>(shopIds);

        StaffDTO staffDTO;
        try {
            staffDTO = staffService.findActiveByStaffCode(staffCode);
        } catch (BusinessException e) {
            throw new BusinessException("BCCS-ORGANIZATION-STAFF-0004",
                    "Mã user " + staffCode
                            + " không tồn tại hoặc đang không ở trạng thái hoạt động. Vui lòng kiểm tra lại");

        }

        ShopDTO shopDTO = null;
        if (staffDTO.getShopId() != null) {
            try {
                shopDTO = shopService.getActiveById(staffDTO.getShopId());
            } catch (BusinessException e) {
                log.warn("Không tìm thấy cửa hàng active của user, bỏ qua kho đơn vị: {}", staffDTO.getShopId());
            }
        }

        List<StockDTO> resultList = new ArrayList<>();
        // Kho cá nhân (type 2) = mã user — hợp lệ sẵn, không check mapping
        if (remaining.remove(staffDTO.getStaffId())) {
            resultList.add(new StockDTO(staffDTO.getStaffId(), staffDTO.getStaffCode(), staffDTO.getName(), "2"));
        }
        // Kho đơn vị (type 1) = mã shop của user — hợp lệ sẵn, không check mapping
        if (shopDTO != null && remaining.remove(shopDTO.getShopId())) {
            resultList.add(new StockDTO(shopDTO.getShopId(), shopDTO.getShopCode(), shopDTO.getName(), "1"));
        }
        // Các shopId còn lại: kho chức năng → phải mapping hợp lệ trong STOCK_CHANNEL_MAPPING
        if (!remaining.isEmpty() && shopDTO != null && shopDTO.getChannelTypeId() != null
                && staffDTO.getShopId() != null) {
            List<Long> grantedStockIds = stockChannelMappingService.findActiveFunctionalStockIds(
                    telServiceId, shopDTO.getChannelTypeId(), staffDTO.getShopId(), staffDTO.getStaffId());
            Set<Long> validFunctional = new HashSet<>(grantedStockIds);
            if (!validFunctional.contains(ALL_STOCKS)) {
                remaining.removeIf(id -> !validFunctional.contains(id));
            }
            if (remaining == null || remaining.isEmpty()) {
                throw new BusinessException("BCCS-ORGANIZATION-STOCK-0002",
                        "Không có kho số chức năng nào được mapping hợp lệ với user " + staffCode
                                + ". Vui lòng kiểm tra lại");

            }
            shopService.findActiveByShopIds(new ArrayList<>(remaining)).forEach(shop ->
                    resultList.add(new StockDTO(shop.getShopId(), shop.getShopCode(), shop.getName(), "3")));
        }
        return resultList;
    }

    private List<StockDTO> resolveFunctionalStocks(StaffDTO staffDTO, ShopDTO shopDTO, Long telServiceId) {
        List<Long> grantedStockIds = new ArrayList<>();
        if (telServiceId != null && shopDTO != null && shopDTO.getChannelTypeId() != null
                && staffDTO.getShopId() != null) {
            grantedStockIds = stockChannelMappingService.findActiveFunctionalStockIds(
                    telServiceId, shopDTO.getChannelTypeId(), staffDTO.getShopId(), staffDTO.getStaffId());
        }
        return toFunctionalStockDtoList(grantedStockIds);
    }

    private List<StockDTO> toFunctionalStockDtoList(List<Long> grantedStockIds) {
        if (grantedStockIds == null || grantedStockIds.isEmpty()) {
            return new ArrayList<>();
        }
        boolean grantAllStocks = grantedStockIds.contains(-1L);
        List<ShopDTO> stocks;
        if (grantAllStocks) {
            stocks = shopService.findActiveByChannelType(Const.ChannelType.STOCK_FUNCTIONAL_CHANNEL);
        } else {
            stocks = shopService.findActiveByShopIds(grantedStockIds);
        }
        return stocks.stream()
                .map(shop -> new StockDTO(shop.getShopId(), shop.getShopCode(), shop.getName(), "3"))
                .toList();
    }

}
