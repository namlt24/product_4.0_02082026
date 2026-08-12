package com.viettel.bccs.productcatalog.productofferprice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của PRODUCT_OFFER_PRICE
 * (xem ProductOfferPriceEntity) — field không nullable ở DB vẫn cho phép null ở DTO,
 * @Size/@Pattern/@Min/@Max chỉ áp dụng khi giá trị khác null.
 */
@Schema(description = "Thông tin giá bán thiết bị")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOfferPriceDTO {

    @Schema(description = "ID giá bán thiết bị", example = "12345")
    @Min(value = 1, message = "productOfferPriceId phải >= 1")
    @Max(value = 999999999999999L, message = "productOfferPriceId vượt quá độ dài cột (precision 15)")
    private Long productOfferPriceId;

    @Schema(description = "ID sản phẩm", example = "456")
    @Min(value = 1, message = "productOfferingId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferingId vượt quá độ dài cột (precision 10)")
    private Long productOfferingId;

    @Schema(description = "ID chính sách giá", example = "7")
    @Min(value = 1, message = "pricePolicyId phải >= 1")
    @Max(value = 9999999999L, message = "pricePolicyId vượt quá độ dài cột (precision 10)")
    private Long pricePolicyId;

    @Schema(description = "ID loại giá", example = "1")
    @Min(value = 1, message = "priceTypeId phải >= 1")
    @Max(value = 9999999999L, message = "priceTypeId vượt quá độ dài cột (precision 10)")
    private Long priceTypeId;

    @Schema(description = "Tên giá bán", example = "Giá bán lẻ thiết bị A")
    @Size(max = 50, message = "name tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Mô tả giá bán", example = "Giá bán lẻ thiết bị cho khách hàng")
    @Size(max = 2000, message = "description tối đa 2000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,2000}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Schema(description = "Giá bán thiết bị", example = "1500000")
    @DecimalMin(value = "0", message = "price phải >= 0")
    @DecimalMax(value = "99999999999999999999", message = "price vượt quá độ dài cột (precision 20)")
    private BigDecimal price;

    @Schema(description = "Giá trị VAT (%)", example = "10")
    @DecimalMin(value = "0", message = "vat phải >= 0")
    @DecimalMax(value = "9999999999", message = "vat vượt quá độ dài cột (precision 10)")
    private BigDecimal vat;

    @Schema(description = "Số tiền ký quỹ", example = "500000")
    @DecimalMin(value = "0", message = "pledgeAmount phải >= 0")
    @DecimalMax(value = "9999999999", message = "pledgeAmount vượt quá độ dài cột (precision 10)")
    private BigDecimal pledgeAmount;

    @Schema(description = "Thời gian ký quỹ (tháng)", example = "12")
    @Min(value = 0, message = "pledgeTime phải >= 0")
    @Max(value = 9999999999L, message = "pledgeTime vượt quá độ dài cột (precision 10)")
    private Long pledgeTime;

    @Schema(description = "Số tiền thanh toán trước (VARCHAR2 trên DB, không phải số)", example = "10")
    @Size(max = 10, message = "priorPay tối đa 10 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,10}$", message = "priorPay không được chứa ký tự điều khiển")
    private String priorPay;

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "status chỉ gồm chữ hoặc số")
    private String status;

    @Schema(description = "Ngày bắt đầu hiệu lực", example = "2026-01-01")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn", example = "2026-12-31")
    private Date expireDatetime;

    @Schema(description = "Độ ưu tiên", example = "1")
    @Min(value = 0, message = "priority phải >= 0")
    @Max(value = 99L, message = "priority vượt quá độ dài cột (precision 2)")
    private Long priority;

    @Schema(description = "Loại hiệu lực (1: theo ngày, 2: theo cron)", example = "1")
    @Size(max = 1, message = "effectType đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "effectType chỉ gồm chữ hoặc số")
    private String effectType;

    @Schema(description = "Biểu thức cron cho hiệu lực tự động", example = "0 0 0 1 * ?")
    @Size(max = 50, message = "cronExpression tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "cronExpression không được chứa ký tự điều khiển")
    private String cronExpression;

    @Schema(description = "Người tạo", example = "admin")
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Ngày tạo", example = "2026-01-01")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "admin")
    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "Ngày cập nhật", example = "2026-06-15")
    private Date updateDatetime;

    @Schema(description = "Mã chương trình", example = "PCCC_2026")
    @Size(max = 50, message = "programCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "programCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String programCode;

    @Schema(description = "Tháng áp dụng chương trình", example = "6")
    @Min(value = 0, message = "programMonth phải >= 0")
    @Max(value = 9999999999L, message = "programMonth vượt quá độ dài cột (precision 10)")
    private Long programMonth;

    @Schema(description = "Có chọn tất cả cửa hàng không", example = "true")
    @Size(max = 10, message = "isSelectAllShop tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "isSelectAllShop chỉ gồm chữ, số, '_' hoặc '-'")
    private String isSelectAllShop;

    @Schema(description = "Giới hạn số lượng", example = "0")
    @Min(value = 0, message = "limited phải >= 0")
    @Max(value = 9L, message = "limited vượt quá độ dài cột (precision 1)")
    private Long limited;

    @Schema(description = "Tên sản phẩm")
    @Size(max = 500, message = "productOfferName tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "productOfferName không được chứa ký tự điều khiển")
    private String productOfferName;

    @Schema(description = "Giá thiết bị khuyến mãi (từ SensorFeeRule)")
    @Min(value = 0, message = "priceEquipment phải >= 0")
    @Max(value = 999999999999999L, message = "priceEquipment vượt quá độ dài cột (precision 15)")
    private Long priceEquipment;
}