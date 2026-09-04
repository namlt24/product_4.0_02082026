package com.viettel.bccs.productcatalog.client;

import java.util.List;

import com.viettel.bccs.productcatalog.client.dto.ShopDTO;
import com.viettel.bccs.productcatalog.client.dto.StaffShopResponse;

public interface StaffShopClient {

    /**
     * Tìm danh sách cửa hàng active theo danh sách shopId.
     * Gọi sang organization-resource-service để lấy thông tin shop.
     *
     * @param shopIds danh sách shopId cần lọc
     * @return danh sách ShopDTO active
     */
    List<ShopDTO> findActiveByShopIds(List<Long> shopIds);

    /**
     * Tìm thông tin nhân viên kèm cửa hàng theo mã nhân viên.
     * Gọi sang organization-resource-service để lấy thông tin nhân viên + shop.
     *
     * @param staffCode mã nhân viên
     * @return StaffShopResponse, hoặc null nếu không tìm thấy/lỗi gọi
     */
    StaffShopResponse getStaffShopFullInfo(String staffCode);
}
