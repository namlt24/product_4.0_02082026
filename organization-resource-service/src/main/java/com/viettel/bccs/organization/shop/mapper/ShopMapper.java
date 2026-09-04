package com.viettel.bccs.organization.shop.mapper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.organization.shop.dto.ShopDTO;
import com.viettel.bccs.organization.shop.entity.ShopEntity;

@Component
public class ShopMapper {

    public ShopDTO toResponse(ShopEntity entity) {
        ShopDTO response = new ShopDTO();
        response.setShopId(entity.getShopId());
        response.setName(entity.getName());
        response.setParentShopId(entity.getParentShopId());
        response.setAccount(entity.getAccount());
        response.setBankName(entity.getBankName());
        response.setAddress(entity.getAddress());
        response.setTel(entity.getTel());
        response.setFax(entity.getFax());
        response.setShopCode(entity.getShopCode());
        response.setShopType(entity.getShopType());
        response.setContactName(entity.getContactName());
        response.setContactTitle(entity.getContactTitle());
        response.setTelNumber(entity.getTelNumber());
        response.setEmail(entity.getEmail());
        response.setDescription(entity.getDescription());
        response.setProvince(entity.getProvince());
        response.setParShopCode(entity.getParShopCode());
        response.setCenterCode(entity.getCenterCode());
        response.setOldShopCode(entity.getOldShopCode());
        response.setCompany(entity.getCompany());
        response.setTin(entity.getTin());
        response.setShop(entity.getShop());
        response.setProvinceCode(entity.getProvinceCode());
        response.setPayComm(entity.getPayComm());
        response.setCreateDate(entity.getCreateDate());
        response.setChannelTypeId(entity.getChannelTypeId());
        response.setDiscountPolicy(entity.getDiscountPolicy());
        response.setPricePolicy(entity.getPricePolicy());
        response.setShopPath(entity.getShopPath());
        response.setDistrict(entity.getDistrict());
        response.setPrecinct(entity.getPrecinct());
        response.setAreaCode(entity.getAreaCode());
        response.setIdNo(entity.getIdNo());
        response.setIdIssuePlace(entity.getIdIssuePlace());
        response.setIdIssueDate(entity.getIdIssueDate());
        response.setStreetBlock(entity.getStreetBlock());
        response.setStreet(entity.getStreet());
        response.setHome(entity.getHome());
        response.setIdType(entity.getIdType());
        response.setShopPathName(entity.getShopPathName());
        response.setContractNo(entity.getContractNo());
        response.setFileName(entity.getFileName());
        response.setBusinessLicence(entity.getBusinessLicence());
        response.setBankplusMobile(entity.getBankplusMobile());
        response.setStockNum(entity.getStockNum());
        response.setStockNumImp(entity.getStockNumImp());
        response.setUpdateDateTime(entity.getUpdateDateTime());
        response.setBankCode(entity.getBankCode());
        response.setShopKeeperId(entity.getShopKeeperId());
        response.setShopDirectorId(entity.getShopDirectorId());
        response.setGroupChannelTypeId(entity.getGroupChannelTypeId());
        response.setStaffOwnerId(entity.getStaffOwnerId());
        response.setTenantId(entity.getTenantId());
        response.setBusinessProvince(entity.getBusinessProvince());
        response.setBusinessDistrict(entity.getBusinessDistrict());
        response.setBusinessPrecinct(entity.getBusinessPrecinct());
        response.setBusinessStreetBlock(entity.getBusinessStreetBlock());
        response.setBusinessStreet(entity.getBusinessStreet());
        response.setBusinessHome(entity.getBusinessHome());
        response.setBusinessAreacode(entity.getBusinessAreacode());
        response.setBusinessAddress(entity.getBusinessAddress());
        response.setCreateUser(entity.getCreateUser());
        response.setUpdateUser(entity.getUpdateUser());
        response.setCreateDatetime(entity.getCreateDatetime());
        response.setStatus(entity.getStatus());
        response.setTurnover(entity.getTurnover());
        response.setBirthday(entity.getBirthday());
        return response;
    }
}
