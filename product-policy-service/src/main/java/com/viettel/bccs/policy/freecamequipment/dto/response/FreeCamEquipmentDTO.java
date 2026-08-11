package com.viettel.bccs.policy.freecamequipment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Migrate từ mono: FreeCamEquipmentDTO, dùng bởi getPriceInServices (nhánh giá thiết bị CAM).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class FreeCamEquipmentDTO {

    @Schema(description = "ID thiết bị CAM miễn phí", example = "1")
    private Long freeCamEquipmentId;

    @Schema(description = "Mã tác động", example = "5001")
    private String actionCode;

    @Schema(description = "ID lý do", example = "9003997310")
    private Long reasonId;

    @Schema(description = "Mã địa bàn", example = "H004")
    private String areaCode;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Số lượng CAM trong nhà", example = "2")
    private Long camInsideNumber;

    @Schema(description = "Số lượng CAM ngoài trời", example = "3")
    private Long camOutsideNumber;

    @Schema(description = "Số lượng CAM tối đa", example = "5")
    private Long camMaxNumber;

    @Schema(description = "Giá thiết bị CAM trong nhà", example = "88888")
    private Long camInsidePrice;

    @Schema(description = "Giá thiết bị CAM ngoài trời", example = "99999")
    private Long camOutsidePrice;

    @Schema(description = "Ngày hiệu lực")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hiệu lực")
    private Date expireDatetime;

    @Schema(description = "Người tạo")
    private String createUser;

    @Schema(description = "Người cập nhật")
    private String updateUser;

    @Schema(description = "Mô tả")
    private String description;

    @Schema(description = "Mã cửa hàng")
    private String shopCode;

    @Schema(description = "Mã nhân viên")
    private String staffCode;

    @Schema(description = "Ngày tạo")
    private Date createDatetime;

    @Schema(description = "Ngày cập nhật")
    private Date updateDatetime;

    @Schema(description = "Nhóm khách hàng")
    private String customerGroup;

    @Schema(description = "Loại khách hàng")
    private String customerType;
}
