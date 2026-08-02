package com.viettel.bccs.productcatalog.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Schema(description = "Thông tin quy tắc phí cảm biến")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorFeeRuleDTO {

    @Schema(description = "ID quy tắc phí cảm biến", example = "1")
    @JsonProperty("SENSOR_FEE_RULE_ID")
    private Long sensorFeeRuleId;

    @Schema(description = "Mã hành động", example = "ACTION_01")
    @JsonProperty("ACTION_CODE")
    private String actionCode;

    @Schema(description = "ID lý do", example = "5")
    @JsonProperty("REASON_ID")
    private Long reasonId;

    @Schema(description = "Mã vùng", example = "HCM")
    @JsonProperty("AREA_CODE")
    private String areaCode;

    @Schema(description = "Số lượng cảm biến tối đa", example = "10")
    @JsonProperty("SENSOR_MAX_NUMBER")
    private Long sensorMaxNumber;

    @Schema(description = "Giá khuyến mãi", example = "100000")
    @JsonProperty("PROMOTIONAL_PRICE")
    private Long promotionalPrice;

    @Schema(description = "Ngày bắt đầu hiệu lực")
    @JsonProperty("EFFECT_DATETIME")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn")
    @JsonProperty("EXPIRE_DATETIME")
    private Date expireDatetime;

    @Schema(description = "Trạng thái", example = "1")
    @JsonProperty("STATUS")
    private String status;

    @Schema(description = "Mã cửa hàng")
    @JsonProperty("SHOP_CODE")
    private String shopCode;

    @Schema(description = "Mã nhân viên")
    @JsonProperty("STAFF_CODE")
    private String staffCode;

    @Schema(description = "Ghi chú")
    @JsonProperty("NOTE")
    private String note;

    @Schema(description = "Nhóm khách hàng")
    @JsonProperty("CUSTOMER_GROUP")
    private String customerGroup;

    @Schema(description = "Người tạo")
    @JsonProperty("CREATE_USER")
    private String createUser;

    @Schema(description = "Người cập nhật")
    @JsonProperty("UPDATE_USER")
    private String updateUser;

    @Schema(description = "Ngày tạo")
    @JsonProperty("CREATE_DATETIME")
    private Date createDatetime;

    @Schema(description = "Ngày cập nhật")
    @JsonProperty("UPDATE_DATETIME")
    private Date updateDatetime;

    @Schema(description = "Loại khách hàng")
    @JsonProperty("CUSTOMER_TYPE")
    private String customerType;

    @Schema(description = "ID dịch vụ viễn thông", example = "1")
    @JsonProperty("TELECOM_SERVICE_ID")
    private Long telecomServiceId;
}