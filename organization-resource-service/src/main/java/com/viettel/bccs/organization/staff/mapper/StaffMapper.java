package com.viettel.bccs.organization.staff.mapper;

import com.viettel.bccs.organization.shop.entity.ShopEntity;
import com.viettel.bccs.organization.shop.dto.response.ShopResponse;
import com.viettel.bccs.organization.staff.dto.StaffDTO;
import com.viettel.bccs.organization.staff.dto.response.StaffResponse;
import com.viettel.bccs.organization.staff.entity.StaffEntity;
import org.springframework.stereotype.Component;

@Component
public class StaffMapper {

    public StaffDTO toDTO(StaffEntity entity) {
        StaffDTO dto = new StaffDTO();
        dto.setStaffId(entity.getStaffId());
        dto.setShopId(entity.getShopId());
        dto.setStaffCode(entity.getStaffCode());
        dto.setName(entity.getName());
        dto.setBirthday(entity.getBirthday());
        dto.setIdNo(entity.getIdNo());
        dto.setIdIssuePlace(entity.getIdIssuePlace());
        dto.setIdIssueDate(entity.getIdIssueDate());
        dto.setTel(entity.getTel());
        dto.setType(entity.getType());
        dto.setSerial(entity.getSerial());
        dto.setIsdn(entity.getIsdn());
        dto.setPin(entity.getPin());
        dto.setAddress(entity.getAddress());
        dto.setProvince(entity.getProvince());
        dto.setStaffOwnType(entity.getStaffOwnType());
        dto.setStaffOwnerId(entity.getStaffOwnerId());
        dto.setChannelTypeId(entity.getChannelTypeId());
        dto.setPricePolicy(entity.getPricePolicy());
        dto.setDiscountPolicy(entity.getDiscountPolicy());
        dto.setPointOfSale(entity.getPointOfSale());
        dto.setLockStatus(entity.getLockStatus());
        dto.setLastLockTime(entity.getLastLockTime());
        dto.setImsi(entity.getImsi());
        dto.setPaymentLimit(entity.getPaymentLimit());
        dto.setPaymentUsage(entity.getPaymentUsage());
        dto.setNote(entity.getNote());
        dto.setBusinessMethod(entity.getBusinessMethod() != null ? entity.getBusinessMethod().longValue() : null);
        dto.setContractMethod(entity.getContractMethod() != null ? entity.getContractMethod().longValue() : null);
        dto.setHasEquipment(entity.getHasEquipment() != null ? entity.getHasEquipment().longValue() : null);
        dto.setHasTin(entity.getHasTin() != null ? entity.getHasTin().longValue() : null);
        dto.setTin(entity.getTin());
        dto.setDistrict(entity.getDistrict());
        dto.setPrecinct(entity.getPrecinct());
        dto.setAreaCode(entity.getAreaCode());
        dto.setStreetBlock(entity.getStreetBlock());
        dto.setStreet(entity.getStreet());
        dto.setHome(entity.getHome());
        dto.setStockNumImp(entity.getStockNumImp() != null ? entity.getStockNumImp().longValue() : null);
        dto.setStockNum(entity.getStockNum() != null ? entity.getStockNum().longValue() : null);
        dto.setPointOfSaleType(entity.getPointOfSaleType() != null ? entity.getPointOfSaleType().longValue() : null);
        dto.setTtnsCode(entity.getTtnsCode());
        dto.setRegisterInfo(entity.getRegisterInfo());
        dto.setIdType(entity.getIdType() != null ? entity.getIdType().longValue() : null);
        dto.setLockFlag(entity.getLockFlag());
        dto.setWarningContent(entity.getWarningContent());
        dto.setContractNo(entity.getContractNo());
        dto.setContractFromDate(entity.getContractFromDate());
        dto.setContractToDate(entity.getContractToDate());
        dto.setDepositValue(entity.getDepositValue());
        dto.setBtsCode(entity.getBtsCode());
        dto.setFileName(entity.getFileName());
        dto.setBankplusMobile(entity.getBankplusMobile());
        dto.setEmail(entity.getEmail());
        dto.setBusinessLicence(entity.getBusinessLicence());
        dto.setSubOwnerType(entity.getSubOwnerType() != null ? entity.getSubOwnerType().longValue() : null);
        dto.setSubOwnerId(entity.getSubOwnerId());
        dto.setLastModified(entity.getLastModified());
        dto.setShopOwnerId(entity.getShopOwnerId());
        dto.setUserId(entity.getUserId());
        dto.setGroupChannelTypeId(entity.getGroupChannelTypeId());
        dto.setBankplusBankCode(entity.getBankplusBankCode());
        dto.setGender(entity.getGender() != null ? entity.getGender().longValue() : null);
        dto.setTenantId(entity.getTenantId());
        dto.setCreateUser(entity.getCreateUser());
        dto.setUpdateUser(entity.getUpdateUser());
        dto.setCreateDatetime(entity.getCreateDatetime());
        dto.setUpdateDatetime(entity.getUpdateDatetime());
        dto.setStatus(entity.getStatus());
        dto.setMapAreaChainChannel(entity.getMapAreaChainChannel());
        return dto;
    }

    public StaffResponse toResponse(StaffDTO dto, ShopResponse shopResponse) {
        StaffResponse response = StaffResponse.builder()
                .staffId(dto.getStaffId())
                .staffCode(dto.getStaffCode())
                .name(dto.getName())
                .tel(dto.getTel())
                .email(dto.getEmail())
                .idNo(dto.getIdNo())
                .idIssuePlace(dto.getIdIssuePlace())
                .province(dto.getProvince())
                .district(dto.getDistrict())
                .precinct(dto.getPrecinct())
                .areaCode(dto.getAreaCode())
                .address(dto.getAddress())
                .shopId(dto.getShopId())
                .status(dto.getStatus())
                .channelTypeId(dto.getChannelTypeId())
                .type(dto.getType())
                .userId(dto.getUserId())
                .staffOwnerId(dto.getStaffOwnerId())
                .staffOwnType(dto.getStaffOwnType())
                .shop(shopResponse)
                .build();
        if (dto.getBirthday() != null) {
            response.setBirthday(dto.getBirthday());
        }
        if (dto.getIdIssueDate() != null) {
            response.setIdIssueDate(dto.getIdIssueDate());
        }
        return response;
    }

    public StaffResponse toResponse(StaffEntity entity, ShopResponse shopResponse) {
        return toResponse(toDTO(entity), shopResponse);
    }

    public ShopResponse toShopResponse(ShopEntity entity) {
        if (entity == null) return null;
        return ShopResponse.builder()
                .shopId(entity.getShopId())
                .name(entity.getName())
                .shopCode(entity.getShopCode())
                .parentShopId(entity.getParentShopId())
                .address(entity.getAddress())
                .tel(entity.getTel())
                .email(entity.getEmail())
                .province(entity.getProvince())
                .district(entity.getDistrict())
                .precinct(entity.getPrecinct())
                .channelTypeId(entity.getChannelTypeId())
                .status(entity.getStatus())
                .shopPath(entity.getShopPath())
                .shopType(entity.getShopType())
                .areaCode(entity.getAreaCode())
                .createDatetime(entity.getCreateDatetime())
                .groupChannelTypeId(entity.getGroupChannelTypeId())
                .build();
    }
}