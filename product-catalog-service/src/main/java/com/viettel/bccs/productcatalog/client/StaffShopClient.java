package com.viettel.bccs.productcatalog.client;

import com.viettel.bccs.productcatalog.client.dto.ShopDTO;

import java.util.List;

public interface StaffShopClient {

    /**
     * Tìm danh sách cửa hàng active theo danh sách shopId.
     * Gọi sang organization-resource-service để lấy thông tin shop.
     *
     * @param shopIds danh sách shopId cần lọc
     * @return danh sách ShopDTO active
     */
    List<ShopDTO> findActiveByShopIds(List<Long> shopIds);
}
