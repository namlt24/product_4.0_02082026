package com.viettel.bccs.productcatalog.product.dto.response;

import java.util.List;

import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceResponse;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 1 mặt hàng (product offering) trong kết quả API getListStockTypeWS, kèm danh sách giá bán
 * (bước 8 của flow — thường 0 hoặc 1 phần tử, giữ dạng List để không cắt bớt dữ liệu nếu có
 * nhiều hơn 1 mức giá active thoả điều kiện).
 */
@Schema(description = "Mặt hàng kèm danh sách giá bán")
public record ProductOfferingStockDTO(

        @Schema(description = "ID mặt hàng", example = "456")
        @Min(value = 1, message = "productOfferingId phải >= 1")
        @Max(value = 9999999999L, message = "productOfferingId vượt quá độ dài cột (precision 10)")
        Long productOfferingId,

        @Schema(description = "Mã mặt hàng", example = "IP15PM256")
        @Size(max = 50, message = "code tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "code không được chứa ký tự điều khiển")
        String code,

        @Schema(description = "Tên mặt hàng", example = "iPhone 15 Pro Max 256GB")
        @Size(max = 500, message = "name tối đa 500 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "name không được chứa ký tự điều khiển")
        String name,

        @Schema(description = "Tên loại mặt hàng (\"Mặt hàng\" nếu productOfferTypeId = 7)",
                example = "Thiết bị đầu cuối")
        @Size(max = 50, message = "productTypeName tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "productTypeName không được chứa ký tự điều khiển")
        String productTypeName,

        @Schema(description = "Có kiểm tra serial hay không", example = "1")
        @Min(value = 0, message = "checkSerial phải >= 0")
        @Max(value = 9, message = "checkSerial vượt quá độ dài cột (precision 1)")
        Short checkSerial,

        @Schema(description = "ID dịch vụ viễn thông của mặt hàng", example = "1")
        @Min(value = 1, message = "telecomServiceId phải >= 1")
        @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
        Long telecomServiceId,

        @Schema(description = "Danh sách giá bán (bước 8: nhánh PCCC nếu telecomServiceId=241/254,"
                + " ngược lại nhánh thường)")
        @Size(max = 100, message = "lstProductOfferPrice tối đa 100 phần tử")
        List<ProductOfferPriceResponse> lstProductOfferPrice
) {
}
