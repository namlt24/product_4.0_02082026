package com.viettel.bccs.organization.staff.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.organization.channeltype.dto.ChannelTypeDTO;
import com.viettel.bccs.organization.channeltype.service.ChannelTypeService;
import com.viettel.bccs.organization.client.OptionSetClient;
import com.viettel.bccs.organization.client.dto.OptionSetValueResponse;
import com.viettel.bccs.organization.custtype.dto.CustTypeDTO;
import com.viettel.bccs.organization.custtype.service.CustTypeService;
import com.viettel.bccs.organization.shop.dto.ShopDTO;
import com.viettel.bccs.organization.shop.dto.response.ShopResponse;
import com.viettel.bccs.organization.shop.mapper.ShopMapper;
import com.viettel.bccs.organization.shop.repository.ShopRepository;
import com.viettel.bccs.organization.shop.service.ShopService;
import com.viettel.bccs.organization.staff.dto.StaffDTO;
import com.viettel.bccs.organization.staff.dto.StockDTO;
import com.viettel.bccs.organization.staff.dto.response.StaffResponse;
import com.viettel.bccs.organization.staff.dto.response.StaffSummaryDTO;
import com.viettel.bccs.organization.staff.entity.StaffEntity;
import com.viettel.bccs.organization.staff.mapper.StaffMapper;
import com.viettel.bccs.organization.staff.repository.StaffRepository;
import com.viettel.bccs.organization.utils.Const;
import com.viettel.bccs.organization.utils.RequestValidator;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StaffService {

    private final StaffRepository staffRepository;
    private final ShopRepository shopRepository;
    private final StaffMapper staffMapper;
    private final ShopMapper shopMapper;
    private final ShopService shopService;
    private final OptionSetClient optionSetClient;
    private final CustTypeService custTypeService;
    private final ChannelTypeService channelTypeService;

    public StaffService(StaffRepository staffRepository, ShopRepository shopRepository,
                        StaffMapper staffMapper, ShopMapper shopMapper,
                        @Lazy ShopService shopService,
                        OptionSetClient optionSetClient, CustTypeService custTypeService,
                        ChannelTypeService channelTypeService) {
        this.staffRepository = staffRepository;
        this.shopRepository = shopRepository;
        this.staffMapper = staffMapper;
        this.shopMapper = shopMapper;
        this.shopService = shopService;
        this.optionSetClient = optionSetClient;
        this.custTypeService = custTypeService;
        this.channelTypeService = channelTypeService;
    }

    @Cacheable(value = "staffCache", key = "'STAFF:' + #staffId")
    @Transactional(readOnly = true)
    public StaffDTO getActiveById(Long staffId) {
        log.info("Truy vấn nhân viên active từ DB theo id: {}", staffId);
        return staffRepository.findByStaffIdAndStatus(staffId, Const.Status.ACTIVE)
                .map(staffMapper::toDTO)
                .orElseThrow(() -> new BusinessException("BCCS-ORGANIZATION-STAFF-0001",
                        "Không tìm thấy nhân viên với id: " + staffId));
    }

    @Cacheable(value = "staffCache", key = "'STAFF:' + #staffCode")
    @Transactional(readOnly = true)
    public StaffDTO findActiveByStaffCode(String staffCode) {
        log.info("Truy vấn nhân viên active từ DB theo mã: {}", staffCode);
        return staffRepository.findByStaffCodeAndStatus(staffCode, Const.Status.ACTIVE)
                .map(staffMapper::toDTO)
                .orElseThrow(() -> new BusinessException("BCCS-ORGANIZATION-STAFF-0002",
                        "Không tìm thấy nhân viên với mã: " + staffCode));
    }

    @Transactional(readOnly = true)
    public StaffDTO findActiveByStaffCodeWithChannelOfSalePoint(String staffCode) {
        log.info("Truy vấn nhân viên active theo mã kèm cờ isChannelOfSalePoint: {}", staffCode);
        StaffDTO dto = findActiveByStaffCode(staffCode);
        dto.setIsChannelOfSalePoint(channelTypeService.isChannelOfSalePoint(dto.getChannelTypeId()));
        return dto;
    }

    @Cacheable(value = "staffShopFullInfo", key = "'STAFF_SHOP_FULL:' + #staffCode")
    @Transactional(readOnly = true)
    public StaffResponse getStaffShopFullInfo(String staffCode) {
        log.info("Truy vấn nhân viên và shop active từ DB theo mã: {}", staffCode);

        StaffEntity entity = staffRepository.findByStaffCodeAndStatus(staffCode, Const.Status.ACTIVE)
                .orElseThrow(() -> new BusinessException("BCCS-ORGANIZATION-STAFF-0002",
                        "Không tìm thấy nhân viên với mã: " + staffCode));
        StaffDTO dto = staffMapper.toDTO(entity);

        ShopResponse shopResponse = null;
        if (dto.getShopId() != null) {
            shopResponse = staffMapper.toShopResponse(
                    shopRepository.findByShopIdAndStatus(dto.getShopId(), Const.Status.ACTIVE)
                            .orElse(null));
        }
        return enrichStaffShopResponse(staffMapper.toResponse(dto, shopResponse), shopResponse);
    }

    @Cacheable(value = "staffShopFullInfo", key = "'STAFF_SHOP_FULL_BY_ID:' + #staffId")
    @Transactional(readOnly = true)
    public StaffResponse getStaffShopFullInfoByStaffId(Long staffId) {
        log.info("Truy vấn nhân viên và shop active từ DB theo id: {}", staffId);

        StaffEntity entity = staffRepository.findByStaffIdAndStatus(staffId, Const.Status.ACTIVE)
                .orElseThrow(() -> new BusinessException("BCCS-ORGANIZATION-STAFF-0001",
                        "Không tìm thấy nhân viên với id: " + staffId));
        StaffDTO dto = staffMapper.toDTO(entity);

        ShopResponse shopResponse = null;
        if (dto.getShopId() != null) {
            shopResponse = staffMapper.toShopResponse(
                    shopRepository.findByShopIdAndStatus(dto.getShopId(), Const.Status.ACTIVE)
                            .orElse(null));
        }
        return enrichStaffShopResponse(staffMapper.toResponse(dto, shopResponse), shopResponse);
    }

    private StaffResponse enrichStaffShopResponse(StaffResponse response, ShopResponse shopResponse) {
        if (response == null) {
            return null;
        }
        if (response.getChannelTypeId() != null) {
            try {
                ChannelTypeDTO channelType = channelTypeService.getActiveById(response.getChannelTypeId());
                if (channelType != null) {
                    response.setChannelTypeCode(channelType.getCode());
                    response.setChannelTypeName(channelType.getName());
                }
            } catch (BusinessException e) {
                log.warn("Không tìm thấy loại kênh với id: {}", response.getChannelTypeId());
            }
        }
        if (shopResponse != null && shopResponse.getParentShopId() != null) {
            response.setShopParentId(shopResponse.getParentShopId());
            shopRepository.findByShopIdAndStatus(shopResponse.getParentShopId(), Const.Status.ACTIVE)
                    .map(staffMapper::toShopResponse)
                    .ifPresent(parentShop -> {
                        response.setShopParentCode(parentShop.getShopCode());
                        response.setShopParentName(parentShop.getName());
                    });
        }

        return response;
    }

    @Cacheable(value = "getListStockByStaffCode", key = "'STOCKS:' + #staffCode")
    @Transactional(readOnly = true)
    public List<StockDTO> getListStockByStaffCode(String staffCode) {
        log.info("Lấy danh sách kho theo mã nhân viên: {}", staffCode);
        List<StockDTO> resultList = new ArrayList<>();
        if (staffCode == null || staffCode.isBlank()) {
            return resultList;
        }
        staffCode = staffCode.trim();
        StaffDTO staffDTO;
        try {
            staffDTO = findActiveByStaffCode(staffCode);
        } catch (BusinessException e) {
            log.warn("Không tìm thấy nhân viên với mã: {}", staffCode);
            return resultList;
        }
        ShopDTO shopDTO = staffDTO.getShopId() != null
                ? shopService.getActiveById(staffDTO.getShopId())
                : null;
        List<ShopDTO> allShops = getListCtvStockIsdnMbccs(shopDTO);

        if (allShops != null && allShops.size() > 0) {
            for (ShopDTO item : allShops) {
                StockDTO stockDTO = new StockDTO();
                stockDTO.setStockId(item.getShopId());
                stockDTO.setCode(item.getShopCode());
                stockDTO.setName(item.getName());
                stockDTO.setType("1"); // kho shop
                resultList.add(stockDTO);
            }
        }

        // kho staff
        StockDTO stockDTO = new StockDTO(staffDTO.getStaffId(), staffDTO.getStaffCode(),
                staffDTO.getName(), "2");
        resultList.add(stockDTO);
        return resultList;
    }


    public List<ShopDTO> getListCtvStockIsdnMbccs(ShopDTO shopDTO) {
        log.info("Lấy danh sách kho CTV từ option set: {}", Const.OPTION_SET_CODE_CVS_STOCK_ISDN_VSALE);
        List<ShopDTO> shops = new ArrayList<>();
        List<OptionSetValueResponse> optionSetValues =
                optionSetClient.findValueByOptionSetCode(Const.OPTION_SET_CODE_CVS_STOCK_ISDN_VSALE);
        if (optionSetValues != null && !optionSetValues.isEmpty()) {
            for (OptionSetValueResponse optionSetValue : optionSetValues) {
                ShopDTO item = new ShopDTO();
                item.setShopCode(optionSetValue.getName());
                if (optionSetValue.getValue() != null) {
                    item.setShopId(Long.valueOf(optionSetValue.getValue()));
                }
                item.setName(optionSetValue.getDescription());
                shops.add(item);
            }
        }
        if (shopDTO != null && shopDTO.getShopId() != null
                && shops.stream().noneMatch(x -> shopDTO.getShopId().equals(x.getShopId()))) {
            shops.add(shopDTO);
        }
        return shops;
    }

    @Cacheable(value = "mappingChannelCustTypeV2",
            key = "'MAPPING_CHANNEL_CUST_TYPE_V2:' + #staffCode + ':' + #groupType")
    @Transactional(readOnly = true)
    public List<CustTypeDTO> getMappingChannelCustTypeV2(String staffCode, String groupType) {
        log.info("Truy vấn mapping kênh - loại khách hàng theo staffCode: {}", staffCode);
        if (staffCode == null || staffCode.isBlank()) {
            return null;
        }
        if (groupType == null || groupType.isBlank()) {
            return null;
        }
        StaffResponse staffResponse = getStaffShopFullInfo(staffCode.trim());
        if (staffResponse == null || staffResponse.getShop() == null
                || staffResponse.getShop().getChannelTypeId() == null) {
            return null;
        }
        return custTypeService.getMappingChannelCustType(staffResponse.getShop().getChannelTypeId(), groupType);
    }

    /**
     * Xác định nhân viên duyệt đơn cho một staffCode:
     * B1 kiểm tra đầu vào; B2 lấy thông tin staff + shop (getStaffShopFullInfo);
     * B3 ưu tiên staff_owner_id của staff; B4 thay bằng staff_owner_id của shop;
     * B5 giật lên shop cấp 3 theo shop_path rồi chọn ngẫu nhiên 1 staff trong shop đó.
     * Trả về DTO rút gọn (staffCode, name, staffId); null nếu không tìm được người duyệt.
     */
    @Transactional(readOnly = true)
    public StaffSummaryDTO getApproveStaffOrder(String staffCode) {
        // B1: kiểm tra đầu vào staffCode
        RequestValidator.requireNotBlank(staffCode, "staffCode", "BCCS-PRODUCT-VALIDATE-0000");
        staffCode = staffCode.trim();

        // B2: lấy staff + shop active theo staffCode (ném lỗi nếu không tồn tại)
        StaffResponse staffResponse = getStaffShopFullInfo(staffCode);

        // B3: nhân viên quản lý cấp trên của chính staff
        if (staffResponse.getStaffOwnerId() != null) {
            return staffMapper.toSummary(getActiveById(staffResponse.getStaffOwnerId()));
        }

        // B4: nhân viên quản lý của shop mà staff đang thuộc (STAFF_OWNER_ID trong SHOP)
        ShopResponse shop = staffResponse.getShop();
        if (shop != null && shop.getStaffOwnerId() != null) {
            return staffMapper.toSummary(getActiveById(shop.getStaffOwnerId()));
        }

        // B5: giật lên shop cấp 3 theo shop_path, chọn ngẫu nhiên 1 staff active trong shop đó
        if (shop != null && shop.getShopPath() != null) {
            Long level3ShopId = extractLevel3ShopId(shop.getShopPath());
            if (level3ShopId != null) {
                List<StaffEntity> staffs = staffRepository.findAllByShopIdAndStatus(level3ShopId, Const.Status.ACTIVE);
                if (!staffs.isEmpty()) {
                    StaffEntity random = staffs.get(new Random().nextInt(staffs.size()));
                    return staffMapper.toSummary(random);
                }
            }
        }

        return null;
    }

    /**
     * Từ shop_path dạng "_{shop_id1}_{shop_id2}_{shop_id3}..." lấy ra shop_id của shop cấp 3
     * (segment thứ 3 trong đường dẫn). Trả null nếu đường dẫn không đủ 3 mức hoặc không hợp lệ.
     */
    private Long extractLevel3ShopId(String shopPath) {
        if (shopPath == null || shopPath.isBlank()) {
            return null;
        }
        int level = 0;
        for (String part : shopPath.split("_")) {
            if (part.isBlank()) {
                continue;
            }
            level++;
            if (level == 3) {
                try {
                    return Long.valueOf(part);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }
}
