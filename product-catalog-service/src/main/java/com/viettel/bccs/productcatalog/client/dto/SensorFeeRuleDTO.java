package com.viettel.bccs.productcatalog.client.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Thông tin quy tắc phí cảm biến")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensorFeeRuleDTO {

    @Schema(description = "ID quy tắc phí cảm biến", example = "1")
    private Long sensorFeeRuleId;

    @Schema(description = "Mã hành động", example = "ACTION_01")
    private String actionCode;

    @Schema(description = "ID lý do", example = "5")
    private Long reasonId;

    @Schema(description = "Mã vùng", example = "HCM")
    private String areaCode;

    @Schema(description = "Số lượng cảm biến tối đa", example = "10")
    private Long sensorMaxNumber;

    @Schema(description = "Giá khuyến mãi", example = "100000")
    private Long promotionalPrice;

    @Schema(description = "Ngày bắt đầu hiệu lực")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn")
    private Date expireDatetime;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Mã cửa hàng")
    private String shopCode;

    @Schema(description = "Mã nhân viên")
    private String staffCode;

    @Schema(description = "Ghi chú")
    private String note;

    @Schema(description = "Nhóm khách hàng")
    private String customerGroup;

    @Schema(description = "Người tạo")
    private String createUser;

    @Schema(description = "Người cập nhật")
    private String updateUser;

    @Schema(description = "Ngày tạo")
    private Date createDatetime;

    @Schema(description = "Ngày cập nhật")
    private Date updateDatetime;

    @Schema(description = "Loại khách hàng")
    private String customerType;

    @Schema(description = "ID dịch vụ viễn thông", example = "1")
    private Long telecomServiceId;
}