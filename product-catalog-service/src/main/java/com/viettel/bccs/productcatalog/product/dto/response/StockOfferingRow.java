package com.viettel.bccs.productcatalog.product.dto.response;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Shape nội bộ (không phải API contract công khai) mang 1 dòng kết quả thô của
 * {@code ProductOfferingRepositoryCustom.getListStockModelBySaleServiceCode} — dùng để truyền dữ
 * liệu giữa ProductOfferingService và StockTypeWsService, tránh rò rỉ {@code Object[]} ra khỏi
 * tầng repository. Chưa có giá bán — giá được StockTypeWsService tính riêng ở bước 8 rồi mới dựng
 * response DTO cuối cùng (ProductOfferingStockDTO). Không bao giờ serialize ra ngoài API, nhưng
 * vẫn gắn bound đầy đủ (theo đúng độ dài cột thật) để nhất quán và qua được OpenApiComplianceTest.
 */
public record StockOfferingRow(

        @Min(value = 1, message = "prodPackTypeId phải >= 1")
        @Max(value = 9999999999L, message = "prodPackTypeId vượt quá độ dài cột (precision 10)")
        Long prodPackTypeId,

        @Min(value = 1, message = "productOfferTypeId phải >= 1")
        @Max(value = 9999999999L, message = "productOfferTypeId vượt quá độ dài cột (precision 10)")
        Long productOfferTypeId,

        @Size(max = 50, message = "typeName tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "typeName không được chứa ký tự điều khiển")
        String typeName,

        @Min(value = 0, message = "checkSerial phải >= 0")
        @Max(value = 9, message = "checkSerial vượt quá độ dài cột (precision 1)")
        Short checkSerial,

        @Min(value = 1, message = "productOfferingId phải >= 1")
        @Max(value = 9999999999L, message = "productOfferingId vượt quá độ dài cột (precision 10)")
        Long productOfferingId,

        @Size(max = 50, message = "code tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "code không được chứa ký tự điều khiển")
        String code,

        @Size(max = 500, message = "name tối đa 500 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "name không được chứa ký tự điều khiển")
        String name,

        @Min(value = 1, message = "telecomServiceId phải >= 1")
        @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
        Long telecomServiceId
) {
}
