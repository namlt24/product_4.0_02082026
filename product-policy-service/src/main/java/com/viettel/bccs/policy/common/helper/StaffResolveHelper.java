package com.viettel.bccs.policy.common.helper;

import org.springframework.stereotype.Component;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.client.StaffShopClient;
import com.viettel.bccs.policy.client.dto.StaffDTO;
import com.viettel.bccs.policy.client.dto.StaffResponse;
import com.viettel.bccs.policy.utils.DataUtil;

import lombok.RequiredArgsConstructor;

/**
 * Helper dùng chung để resolve StaffDTO (kèm thông tin shop: shopCode/shopProvince/shopDistrict/
 * shopPrecinct/shopChanelTypeId) từ staffCode, thông qua StaffShopClient.
 * Trước đây logic này bị lặp lại ở nhiều service (MapActiveInfoProductService, ReasonService...),
 * gom về đây để tránh trùng lặp.
 */
@Component
@RequiredArgsConstructor
public class StaffResolveHelper {

    private final StaffShopClient staffShopClient;

    public StaffDTO resolveStaffDTO(String staffCode) {
        StaffResponse staffResponse = staffShopClient.getStaffShopFullInfo(staffCode);
        if (DataUtil.isNullObject(staffResponse)) {
            throw new BusinessException("BCCS-POLICY-MAPACTIVE-0006");
        }
        return toStaffDTO(staffResponse);
    }

    /**
     * Tương tự {@link #resolveStaffDTO(String)} nhưng tra cứu theo staffId (khoá chính STAFF_ID)
     * thay vì staffCode, thông qua {@link StaffShopClient#getStaffShopFullInfoByStaffId(Long)}.
     */
    public StaffDTO resolveStaffDTO(Long staffId) {
        StaffResponse staffResponse = staffShopClient.getStaffShopFullInfoByStaffId(staffId);
        if (DataUtil.isNullObject(staffResponse)) {
            throw new BusinessException("BCCS-POLICY-MAPACTIVE-0006");
        }
        return toStaffDTO(staffResponse);
    }

    private StaffDTO toStaffDTO(StaffResponse staffResponse) {
        StaffDTO staffDTO = staffResponse.toDTO();
        if (!DataUtil.isNullOrEmpty(staffResponse.getShop())) {
            staffDTO.setShopCode(staffResponse.getShop().getShopCode());
            staffDTO.setShopProvince(staffResponse.getShop().getProvince());
            staffDTO.setShopDistrict(staffResponse.getShop().getDistrict());
            staffDTO.setShopPrecinct(staffResponse.getShop().getPrecinct());
            staffDTO.setShopChanelTypeId(staffResponse.getShop().getChannelTypeId());
        }
        return staffDTO;
    }
}
