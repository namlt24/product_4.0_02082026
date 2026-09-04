package com.viettel.bccs.productcatalog.productoffertype.repository;

import java.util.List;

import com.viettel.bccs.productcatalog.productoffertype.entity.ProductOfferTypeEntity;

public interface ProductOfferTypeRepositoryCustom {

    List<ProductOfferTypeEntity> findByIds(List<Long> ids);

    /**
     * Migrate từ mono: ProductOfferTypeServiceImpl.findBySaleServiceCodeWithProductOffering
     * (nhánh containNumber=true — API getListStockTypeWS luôn gọi với true nên KHÔNG replicate
     * nhánh containNumber=false, vốn lọc thêm product_offer_type_id trong 1 danh sách cố định).
     * Lấy danh sách loại mặt hàng (DISTINCT) còn active thuộc gói dịch vụ bán hàng (PRODUCT_PACKAGE,
     * type = SALE_SERVICE) theo saleServiceCode.
     */
    List<ProductOfferTypeEntity> findBySaleServiceCodeWithProductOffering(String saleServiceCode);
}