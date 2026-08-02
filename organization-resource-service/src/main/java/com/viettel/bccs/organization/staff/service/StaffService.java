package com.viettel.bccs.organization.staff.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.organization.client.OptionSetClient;
import com.viettel.bccs.organization.client.dto.OptionSetValueResponse;
import com.viettel.bccs.organization.custtype.dto.CustTypeDTO;
import com.viettel.bccs.organization.custtype.service.CustTypeService;
import com.viettel.bccs.organization.shop.dto.ShopDTO;
import com.viettel.bccs.organization.shop.dto.response.ShopResponse;
import com.viettel.bccs.organization.shop.mapper.ShopMapper;
import com.viettel.bccs.organization.shop.repository.ShopRepository;
import com.viettel.bccs.organization.shop.service.ShopService;
import com.viettel.bccs.organization.staff.dto.StockDTO;
import com.viettel.bccs.organization.staff.dto.StaffDTO;
import com.viettel.bccs.organization.staff.mapper.StaffMapper;
import com.viettel.bccs.organization.staff.entity.StaffEntity;
import com.viettel.bccs.organization.staff.repository.StaffRepository;
import com.viettel.bccs.organization.staff.dto.response.StaffResponse;
import com.viettel.bccs.organization.utils.Const;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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

    public StaffService(StaffRepository staffRepository, ShopRepository shopRepository,
                        StaffMapper staffMapper, ShopMapper shopMapper,
                        @Lazy ShopService shopService,
                        OptionSetClient optionSetClient, CustTypeService custTypeService) {
        this.staffRepository = staffRepository;
        this.shopRepository = shopRepository;
        this.staffMapper = staffMapper;
        this.shopMapper = shopMapper;
        this.shopService = shopService;
        this.optionSetClient = optionSetClient;
        this.custTypeService = custTypeService;
    }

    @Cacheable(value = "staffCache", key = "'STAFF:' + #staffId")
    @Transactional(readOnly = true)
    public StaffDTO getActiveById(Long staffId) {
        log.info("Truy vấn nhân viên active từ DB theo id: {}", staffId);
        return staffRepository.findByStaffIdAndStatus(staffId, Const.STATUS.ACTIVE)
                .map(staffMapper::toDTO)
                .orElseThrow(() -> new BusinessException("ORGANIZATION-STAFF-002", "Không tìm thấy nhân viên với id: " + staffId));
    }

    @Cacheable(value = "staffCache", key = "'STAFF:' + #staffCode")
    @Transactional(readOnly = true)
    public StaffDTO findActiveByStaffCode(String staffCode) {
        log.info("Truy vấn nhân viên active từ DB theo mã: {}", staffCode);
        return staffRepository.findByStaffCodeAndStatus(staffCode, Const.STATUS.ACTIVE)
                .map(staffMapper::toDTO)
                .orElseThrow(() -> new BusinessException("ORGANIZATION-STAFF-002", "Không tìm thấy nhân viên với mã: " + staffCode));
    }

    @Cacheable(value = "staffShopFullInfo", key = "'STAFF_SHOP_FULL:' + #staffCode")
    @Transactional(readOnly = true)
    public StaffResponse getStaffShopFullInfo(String staffCode) {
        log.info("Truy vấn nhân viên và shop active từ DB theo mã: {}", staffCode);

        StaffEntity entity = staffRepository.findByStaffCodeAndStatus(staffCode, Const.STATUS.ACTIVE)
                .orElseThrow(() -> new BusinessException("ORGANIZATION-STAFF-002", "Không tìm thấy nhân viên với mã: " + staffCode));
        StaffDTO dto = staffMapper.toDTO(entity);

        ShopResponse shopResponse = null;
        if (dto.getShopId() != null) {
            shopResponse = staffMapper.toShopResponse(
                    shopRepository.findByShopIdAndStatus(dto.getShopId(), Const.STATUS.ACTIVE)
                            .orElse(null));
        }
        return staffMapper.toResponse(dto, shopResponse);
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
        List<ShopDTO> allShops = getListCTVStockIsdnMbccs(shopDTO);

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

        StockDTO stockDTO = new StockDTO(staffDTO.getStaffId(), staffDTO.getStaffCode(), staffDTO.getName(), "2"); // kho staff
        resultList.add(stockDTO);
        return resultList;
    }


    public List<ShopDTO> getListCTVStockIsdnMbccs(ShopDTO shopDTO) {
        log.info("Lấy danh sách kho CTV từ option set: {}", Const.OPTION_SET_CODE_CVS_STOCK_ISDN_VSALE);
        List<ShopDTO> shops = new ArrayList<>();
        List<OptionSetValueResponse> optionSetValues = optionSetClient.findValueByOptionSetCode(Const.OPTION_SET_CODE_CVS_STOCK_ISDN_VSALE);
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

    @Cacheable(value = "mappingChannelCustTypeV2", key = "'MAPPING_CHANNEL_CUST_TYPE_V2:' + #staffCode + ':' + #groupType")
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
        if (staffResponse == null || staffResponse.getShop() == null || staffResponse.getShop().getChannelTypeId() == null) {
            return null;
        }
        return custTypeService.getMappingChannelCustType(staffResponse.getShop().getChannelTypeId(), groupType);
    }
}