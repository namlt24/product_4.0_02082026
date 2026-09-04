package com.viettel.bccs.productcatalog.product.dto.response;

import java.math.BigDecimal;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của PRODUCT_OFFERING
 * (xem ProductOfferingEntity) — field không nullable ở DB vẫn cho phép null ở response
 * record (record component không có @NotNull), @Size/@Pattern/@Min/@Max chỉ áp dụng khi
 * giá trị khác null.
 */
public record ProductOfferingResponse(

    @Schema(description = "ID sản phẩm", example = "12345")
    @Min(value = 1, message = "productOfferingId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferingId vượt quá độ dài cột (precision 10)")
    Long productOfferingId,

    @Schema(description = "ID đặc tả sản phẩm", example = "100")
    @Min(value = 1, message = "productSpecId phải >= 1")
    @Max(value = 9999999999L, message = "productSpecId vượt quá độ dài cột (precision 10)")
    Long productSpecId,

    @Schema(description = "ID loại sản phẩm", example = "1")
    @Min(value = 1, message = "productOfferTypeId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferTypeId vượt quá độ dài cột (precision 10)")
    Long productOfferTypeId,

    @Schema(description = "Tên sản phẩm", example = "Gói cước data 50GB")
    @Size(max = 500, message = "name tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "name không được chứa ký tự điều khiển")
    String name,

    @Schema(description = "Mã sản phẩm", example = "PACKAGE_001")
    @Size(max = 50, message = "code tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
    String code,

    @Schema(description = "Loại thuê bao (1: trả sau, 2: trả trước)", example = "1")
    @Size(max = 1, message = "subType đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "subType chỉ gồm chữ hoặc số")
    String subType,

    @Schema(description = "ID dịch vụ viễn thông", example = "1")
    @Min(value = 1, message = "telecomServiceId phải >= 1")
    @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
    Long telecomServiceId,

    @Schema(description = "Mô tả sản phẩm")
    @Size(max = 512, message = "description tối đa 512 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
    String description,

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "status chỉ gồm chữ hoặc số")
    String status,

    @Schema(description = "Ngày hiệu lực")
    Date effectDatetime,

    @Schema(description = "Ngày hết hạn")
    Date expireDatetime,

    @Schema(description = "Người tạo", example = "system")
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    String createUser,

    @Schema(description = "Ngày tạo")
    Date createDatetime,

    @Schema(description = "Người cập nhật", example = "system")
    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    String updateUser,

    @Schema(description = "Ngày cập nhật")
    Date updateDatetime,

    @Schema(description = "Phiên bản", example = "1.0")
    @Size(max = 50, message = "version tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "version không được chứa ký tự điều khiển")
    String version,

    @Schema(description = "Kiểm tra serial", example = "1")
    @DecimalMin(value = "0", message = "checkSerial phải >= 0")
    @DecimalMax(value = "9", message = "checkSerial vượt quá độ dài cột (precision 1)")
    BigDecimal checkSerial,

    @Schema(description = "Kiểm tra đặt cọc", example = "1")
    @DecimalMin(value = "0", message = "checkDeposit phải >= 0")
    @DecimalMax(value = "9", message = "checkDeposit vượt quá độ dài cột (precision 1)")
    BigDecimal checkDeposit,

    @Schema(description = "Đơn vị tính", example = "GB")
    @Size(max = 10, message = "unit tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "unit chỉ gồm chữ, số, '_' hoặc '-'")
    String unit,

    @Schema(description = "Mã mô hình kế toán", example = "ACC_MODEL_001")
    @Size(max = 50, message = "accountingModelCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "accountingModelCode chỉ gồm chữ, số, '_' hoặc '-'")
    String accountingModelCode,

    @Schema(description = "Tên mô hình kế toán")
    @Size(max = 100, message = "accountingModelName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "accountingModelName không được chứa ký tự điều khiển")
    String accountingModelName,

    @Schema(description = "Tên kế toán")
    @Size(max = 100, message = "accountingName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "accountingName không được chứa ký tự điều khiển")
    String accountingName,

    @Schema(description = "Mã kế toán", example = "ACC_001")
    @Size(max = 50, message = "accountingCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "accountingCode chỉ gồm chữ, số, '_' hoặc '-'")
    String accountingCode,

    @Schema(description = "Thời lượng demo (ngày)", example = "30")
    @Min(value = 0, message = "demoDuration phải >= 0")
    @Max(value = 9999999999L, message = "demoDuration vượt quá độ dài cột (precision 10)")
    Long demoDuration,

    @Schema(description = "Là gói demo", example = "0")
    @DecimalMin(value = "0", message = "isDemo phải >= 0")
    @DecimalMax(value = "9", message = "isDemo vượt quá độ dài cột (precision 1)")
    BigDecimal isDemo,

    @Schema(description = "Loại thiết bị", example = "MODEM")
    @Size(max = 20, message = "deviceType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "deviceType chỉ gồm chữ, số, '_' hoặc '-'")
    String deviceType,

    @Schema(description = "Bộ thu phát", example = "0")
    @DecimalMin(value = "0", message = "transceiver phải >= 0")
    @DecimalMax(value = "9", message = "transceiver vượt quá độ dài cột (precision 1)")
    BigDecimal transceiver,

    @Schema(description = "Loại mô hình kho", example = "0")
    @DecimalMin(value = "0", message = "stockModelType phải >= 0")
    @DecimalMax(value = "9", message = "stockModelType vượt quá độ dài cột (precision 1)")
    BigDecimal stockModelType,

    @Schema(description = "ID cửa hàng sở hữu", example = "1")
    @Min(value = 1, message = "ownerShopId phải >= 1")
    @Max(value = 9999999999L, message = "ownerShopId vượt quá độ dài cột (precision 10)")
    Long ownerShopId,

    @Schema(description = "Trả hàng khi hủy", example = "N")
    @Size(max = 2, message = "returnStockWhenCancelled tối đa 2 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "returnStockWhenCancelled chỉ gồm chữ và số")
    String returnStockWhenCancelled,

    @Schema(description = "Trả hàng khi hủy (2)", example = "N")
    @Size(max = 2, message = "returnStockWhenCancelled1 tối đa 2 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "returnStockWhenCancelled1 chỉ gồm chữ và số")
    String returnStockWhenCancelled1,

    @Schema(description = "Số vật liệu SAP")
    @DecimalMin(value = "0", message = "sapMaterialNumber phải >= 0")
    @DecimalMax(value = "999999999", message = "sapMaterialNumber vượt quá độ dài cột (precision 9)")
    BigDecimal sapMaterialNumber,

    @Schema(description = "ID mục đích sử dụng")
    @Size(max = 100, message = "usageId tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "usageId chỉ gồm chữ, số, '_' hoặc '-'")
    String usageId,

    @Schema(description = "Phân phối", example = "1")
    @Size(max = 1, message = "distribute đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "distribute chỉ gồm chữ hoặc số")
    String distribute,

    @Schema(description = "Số tháng", example = "12")
    @Min(value = 0, message = "numMonth phải >= 0")
    @Max(value = 9999999999L, message = "numMonth vượt quá độ dài cột (precision 10)")
    Long numMonth,

    @Schema(description = "Là gói bundle", example = "0")
    @Size(max = 10, message = "isBundle tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "isBundle chỉ gồm chữ, số, '_' hoặc '-'")
    String isBundle,

    @Schema(description = "Loại sản phẩm SAP", example = "1")
    @Size(max = 1, message = "sapProductType đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "sapProductType chỉ gồm chữ hoặc số")
    String sapProductType
) {
}
