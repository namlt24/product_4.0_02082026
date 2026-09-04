package com.viettel.bccs.productcatalog.productspecchar.repository;

import java.util.List;

public interface ProductSpecCharRepositoryCustom {

    /**
     * Migrate từ mono: ExternalServiceForMbccsImpl.findProductOfferingByListCodeListSpecCode
     * (ProductSpecCharRepoImpl gốc). Tìm các bản ghi product_spec_char (kèm product_offering chứa
     * nó) khớp ít nhất 1 mã trong lstSpecCode, tuỳ chọn lọc thêm theo lstProductCode. Trả về
     * {@code List<Object[]>} (không map thẳng entity) vì mỗi dòng còn kèm product_offering_id/
     * code/name — không thuộc {@code ProductSpecCharEntity}.
     */
    List<Object[]> findByListSpecCodeAndListProductCode(List<String> lstSpecCode, List<String> lstProductCode,
        Long productOfferTypeId);
}
