package com.viettel.bccs.policy.freecamequipment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Migrate từ mono: FreeCamEquipmentDTO, dùng bởi getPriceInServices (nhánh giá thiết bị CAM).
 * Bound/pattern trên từng field lấy đúng theo độ dài/precision cột thật của FREE_CAM_EQUIPMENT
 * (xem FreeCamEquipmentEntity).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class FreeCamEquipmentDTO {

    @Schema(description = "ID thiết bị CAM miễn phí", example = "1")
    @Min(value = 0, message = "freeCamEquipmentId phải >= 0")
    @Max(value = 9999999999L, message = "freeCamEquipmentId vượt quá độ dài cột (precision 10)")
    private Long freeCamEquipmentId;

    @Schema(description = "Mã tác động", example = "5001")
    @Size(max = 10, message = "actionCode tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,10}$", message = "actionCode chỉ gồm chữ và số")
    private String actionCode;

    @Schema(description = "ID lý do", example = "9003997310")
    @Min(value = 0, message = "reasonId phải >= 0")
    @Max(value = 9999999999L, message = "reasonId vượt quá độ dài cột (precision 10)")
    private Long reasonId;

    @Schema(description = "Mã địa bàn", example = "H004")
    @Size(max = 200, message = "areaCode tối đa 200 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,200}$", message = "areaCode chỉ gồm chữ và số")
    private String areaCode;

    @Schema(description = "Trạng thái", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Số lượng CAM trong nhà", example = "2")
    @Min(value = 0, message = "camInsideNumber phải >= 0")
    @Max(value = 9999999999L, message = "camInsideNumber vượt quá độ dài cột (precision 10)")
    private Long camInsideNumber;

    @Schema(description = "Số lượng CAM ngoài trời", example = "3")
    @Min(value = 0, message = "camOutsideNumber phải >= 0")
    @Max(value = 9999999999L, message = "camOutsideNumber vượt quá độ dài cột (precision 10)")
    private Long camOutsideNumber;

    @Schema(description = "Số lượng CAM tối đa", example = "5")
    @Min(value = 0, message = "camMaxNumber phải >= 0")
    @Max(value = 9999999999L, message = "camMaxNumber vượt quá độ dài cột (precision 10)")
    private Long camMaxNumber;

    @Schema(description = "Giá thiết bị CAM trong nhà", example = "88888")
    @Min(value = 0, message = "camInsidePrice phải >= 0")
    @Max(value = 9999999999L, message = "camInsidePrice vượt quá độ dài cột (precision 10)")
    private Long camInsidePrice;

    @Schema(description = "Giá thiết bị CAM ngoài trời", example = "99999")
    @Min(value = 0, message = "camOutsidePrice phải >= 0")
    @Max(value = 9999999999L, message = "camOutsidePrice vượt quá độ dài cột (precision 10)")
    private Long camOutsidePrice;

    @Schema(description = "Ngày hiệu lực")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hiệu lực")
    private Date expireDatetime;

    @Schema(description = "Người tạo")
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Người cập nhật")
    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "Mô tả")
    @Size(max = 4000, message = "description tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Schema(description = "Mã cửa hàng")
    @Size(max = 40, message = "shopCode tối đa 40 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "shopCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopCode;

    @Schema(description = "Mã nhân viên")
    @Size(max = 4000, message = "staffCode tối đa 4000 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9,;_\\- ]{0,4000}$", message = "staffCode chỉ gồm chữ, số, ',', ';', '_', '-' hoặc khoảng trắng")
    private String staffCode;

    @Schema(description = "Ngày tạo")
    private Date createDatetime;

    @Schema(description = "Ngày cập nhật")
    private Date updateDatetime;

    @Schema(description = "Nhóm khách hàng")
    @Size(max = 4000, message = "customerGroup tối đa 4000 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9,;_\\- ]{0,4000}$", message = "customerGroup chỉ gồm chữ, số, ',', ';', '_', '-' hoặc khoảng trắng")
    private String customerGroup;

    @Schema(description = "Loại khách hàng")
    @Size(max = 4000, message = "customerType tối đa 4000 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9,;_\\- ]{0,4000}$", message = "customerType chỉ gồm chữ, số, ',', ';', '_', '-' hoặc khoảng trắng")
    private String customerType;
}
