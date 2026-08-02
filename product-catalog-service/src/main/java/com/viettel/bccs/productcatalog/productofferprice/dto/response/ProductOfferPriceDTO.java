package com.viettel.bccs.productcatalog.productofferprice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("PRODUCT_OFFER_PRICE_ID")
    private Long productOfferPriceId;

    @Schema(description = "ID sản phẩm", example = "456")
    @JsonProperty("PRODUCT_OFFERING_ID")
    private Long productOfferingId;

    @Schema(description = "ID chính sách giá", example = "7")
    @JsonProperty("PRICE_POLICY_ID")
    private Long pricePolicyId;

    @Schema(description = "ID loại giá", example = "1")
    @JsonProperty("PRICE_TYPE_ID")
    private Long priceTypeId;

    @Schema(description = "Tên giá bán", example = "Giá bán lẻ thiết bị A")
    @JsonProperty("NAME")
    private String name;

    @Schema(description = "Mô tả giá bán", example = "Giá bán lẻ thiết bị cho khách hàng")
    @JsonProperty("DESCRIPTION")
    private String description;

    @Schema(description = "Giá bán thiết bị", example = "1500000")
    @JsonProperty("PRICE")
    private BigDecimal price;

    @Schema(description = "Giá trị VAT (%)", example = "10")
    @JsonProperty("VAT")
    private BigDecimal vat;

    @Schema(description = "Số tiền ký quỹ", example = "500000")
    @JsonProperty("PLEDGE_AMOUNT")
    private BigDecimal pledgeAmount;

    @Schema(description = "Thời gian ký quỹ (tháng)", example = "12")
    @JsonProperty("PLEDGE_TIME")
    private Long pledgeTime;

    @Schema(description = "Số tiền thanh toán trước", example = "300000")
    @JsonProperty("PRIOR_PAY")
    private BigDecimal priorPay;

    @Schema(description = "Trạng thái", example = "1")
    @JsonProperty("STATUS")
    private String status;

    @Schema(description = "Ngày bắt đầu hiệu lực", example = "2026-01-01")
    @JsonProperty("EFFECT_DATETIME")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn", example = "2026-12-31")
    @JsonProperty("EXPIRE_DATETIME")
    private Date expireDatetime;

    @Schema(description = "Độ ưu tiên", example = "1")
    @JsonProperty("PRIORITY")
    private Long priority;

    @Schema(description = "Loại hiệu lực (1: theo ngày, 2: theo cron)", example = "1")
    @JsonProperty("EFFECT_TYPE")
    private String effectType;

    @Schema(description = "Biểu thức cron cho hiệu lực tự động", example = "0 0 0 1 * ?")
    @JsonProperty("CRON_EXPRESSION")
    private String cronExpression;

    @Schema(description = "Người tạo", example = "admin")
    @JsonProperty("CREATE_USER")
    private String createUser;

    @Schema(description = "Ngày tạo", example = "2026-01-01")
    @JsonProperty("CREATE_DATETIME")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "admin")
    @JsonProperty("UPDATE_USER")
    private String updateUser;

    @Schema(description = "Ngày cập nhật", example = "2026-06-15")
    @JsonProperty("UPDATE_DATETIME")
    private Date updateDatetime;

    @Schema(description = "Mã chương trình", example = "PCCC_2026")
    @JsonProperty("PROGRAM_CODE")
    private String programCode;

    @Schema(description = "Tháng áp dụng chương trình", example = "6")
    @JsonProperty("PROGRAM_MONTH")
    private Long programMonth;

    @Schema(description = "Có chọn tất cả cửa hàng không", example = "true")
    @JsonProperty("IS_SELECT_ALL_SHOP")
    private String isSelectAllShop;

    @Schema(description = "Giới hạn số lượng", example = "0")
    @JsonProperty("LIMITED")
    private Long limited;

    @Schema(description = "Tên sản phẩm")
    @JsonProperty("PRODUCT_OFFER_NAME")
    private String productOfferName;

    @Schema(description = "Giá thiết bị khuyến mãi (từ SensorFeeRule)")
    @JsonProperty("PRICE_EQUIPMENT")
    private Long priceEquipment;
}