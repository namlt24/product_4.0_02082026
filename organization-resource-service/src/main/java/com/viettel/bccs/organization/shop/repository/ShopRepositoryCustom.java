package com.viettel.bccs.organization.shop.repository;

import com.viettel.bccs.organization.shop.entity.ShopEntity;

import java.util.List;

/**
 * Custom repository cho Shop - hỗ trợ các truy vấn phức tạp không thể biểu diễn bằng Spring Data JPA đơn thuần.
 */
public interface ShopRepositoryCustom {

    /**
     * Tìm danh sách ShopEntity theo danh sách shopId với status = 1.
     * Query được thực hiện theo batch để tránh ORA-01795 khi danh sách shopIds lớn.
     *
     * @param shopIds danh sách shopId cần lọc
     * @return danh sách ShopEntity active, không trùng lặp
     */
    List<ShopEntity> findActiveByShopIds(List<Long> shopIds);
}