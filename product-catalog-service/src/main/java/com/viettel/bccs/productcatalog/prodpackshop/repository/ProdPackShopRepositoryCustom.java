package com.viettel.bccs.productcatalog.prodpackshop.repository;

import java.util.List;
import java.util.Map;

public interface ProdPackShopRepositoryCustom {

    /**
     * Tìm danh sách shopId theo danh sách prodPackTypeId từ bảng PROD_PACK_SHOP.
     * Query được thực hiện theo batch để tránh ORA-01795 khi danh sách lớn.
     *
     * @param prodPackTypeIds danh sách prodPackTypeId cần lọc
     * @return Map với key = prodPackTypeId, value = danh sách shopId tương ứng
     */
    Map<Long, List<Long>> findShopIdsByProdPackTypeIds(List<Long> prodPackTypeIds);
}