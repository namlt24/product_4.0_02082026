package com.viettel.bccs.productcatalog.productofferprice.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Schema(description = "Thông tin giá bán thiết bị")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOfferPriceDTO {

    @Schema(description = "ID giá bán thiết bị", example = "12345")
    private Long productOfferPriceId;

    @Schema(description = "ID sản phẩm", example = "456")
    private Long productOfferingId;

    @Schema(description = "ID chính sách giá", example = "7")
    private Long pricePolicyId;

    @Schema(description = "ID loại giá", example = "1")
    private Long priceTypeId;

    @Schema(description = "Tên giá bán", example = "Giá bán lẻ thiết bị A")
    private String name;

    @Schema(description = "Mô tả giá bán", example = "Giá bán lẻ thiết bị cho khách hàng")
    private String description;

    @Schema(description = "Giá bán thiết bị", example = "1500000")
    private BigDecimal price;

    @Schema(description = "Giá trị VAT (%)", example = "10")
    private BigDecimal vat;

    @Schema(description = "Số tiền ký quỹ", example = "500000")
    private BigDecimal pledgeAmount;

    @Schema(description = "Thời gian ký quỹ (tháng)", example = "12")
    private Long pledgeTime;

    @Schema(description = "Số tiền thanh toán trước", example = "300000")
    private BigDecimal priorPay;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Ngày bắt đầu hiệu lực", example = "2026-01-01")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn", example = "2026-12-31")
    private Date expireDatetime;

    @Schema(description = "Độ ưu tiên", example = "1")
    private Long priority;

    @Schema(description = "Loại hiệu lực (1: theo ngày, 2: theo cron)", example = "1")
    private String effectType;

    @Schema(description = "Biểu thức cron cho hiệu lực tự động", example = "0 0 0 1 * ?")
    private String cronExpression;

    @Schema(description = "Người tạo", example = "admin")
    private String createUser;

    @Schema(description = "Ngày tạo", example = "2026-01-01")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "admin")
    private String updateUser;

    @Schema(description = "Ngày cập nhật", example = "2026-06-15")
    private Date updateDatetime;

    @Schema(description = "Mã chương trình", example = "PCCC_2026")
    private String programCode;

    @Schema(description = "Tháng áp dụng chương trình", example = "6")
    private Long programMonth;

    @Schema(description = "Có chọn tất cả cửa hàng không", example = "true")
    private String isSelectAllShop;

    @Schema(description = "Giới hạn số lượng", example = "0")
    private Long limited;

    @Schema(description = "Tên sản phẩm")
    private String productOfferName;

    @Schema(description = "Giá thiết bị khuyến mãi (từ SensorFeeRule)")
    private Long priceEquipment;
}