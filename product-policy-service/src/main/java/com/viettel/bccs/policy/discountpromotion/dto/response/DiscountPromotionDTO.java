package com.viettel.bccs.policy.discountpromotion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DiscountPromotionDTO {

    @Schema(description = "Id khuyến mãi giảm giá (PK)", example = "1")
    @Min(value = 0, message = "discountPromotionId phải >= 0")
    @Max(value = 9999999999L, message = "discountPromotionId vượt quá độ dài cột (precision 10)")
    private Long discountPromotionId;

    @Schema(description = "Id dịch vụ viễn thông", example = "100")
    @Size(max = 100, message = "telecomServiceId tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,100}$", message = "telecomServiceId chỉ gồm chữ và số")
    private String telecomServiceId;

    @Schema(description = "Mã khuyến mãi", example = "KM001")
    @Size(max = 20, message = "code tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
    private String code;

    @Schema(description = "Tên khuyến mãi", example = "Khuyến mãi tháng 8")
    @Size(max = 1500, message = "name tối đa 1500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1500}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Loại khuyến mãi", example = "1")
    @Size(max = 1, message = "type tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "type chỉ gồm chữ và số")
    private String type;

    @Schema(description = "Loại hệ thống", example = "1")
    @Size(max = 1, message = "systemType tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "systemType chỉ gồm chữ và số")
    private String systemType;

    @Schema(description = "Phương thức giảm giá", example = "1")
    @Size(max = 1, message = "discountMethod tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "discountMethod chỉ gồm chữ và số")
    private String discountMethod;

    @Schema(description = "Chính sách giảm giá", example = "POL001")
    @Size(max = 10, message = "discountPolicy tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "discountPolicy chỉ gồm chữ, số, '_' hoặc '-'")
    private String discountPolicy;

    @Schema(description = "Loại phụ", example = "1")
    @Size(max = 1, message = "subType tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "subType chỉ gồm chữ và số")
    private String subType;

    @Schema(description = "Số tháng cam kết", example = "12")
    @Min(value = 0, message = "monthCommitment phải >= 0")
    @Max(value = 9999999999L, message = "monthCommitment vượt quá độ dài cột (precision 10)")
    private Long monthCommitment;

    @Schema(description = "Gói cước", example = "GOI001")
    @Size(max = 30, message = "pricePlan tối đa 30 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,30}$", message = "pricePlan chỉ gồm chữ, số, '_' hoặc '-'")
    private String pricePlan;

    @Schema(description = "Số tiền theo tháng", example = "50000")
    @Min(value = 0, message = "monthAmount phải >= 0")
    @Max(value = 9999999999L, message = "monthAmount vượt quá độ dài cột (precision 10)")
    private Long monthAmount;

    @Schema(description = "Trạng thái (0/1)", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Mô tả")
    @Size(max = 4000, message = "description tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Schema(description = "Nội dung")
    @Size(max = 4000, message = "content tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "content không được chứa ký tự điều khiển")
    private String content;

    @Schema(description = "Danh sách mã địa bàn áp dụng", example = "A076,A077")
    @Size(max = 300, message = "areaCode tối đa 300 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9,_-]{0,300}$", message = "areaCode chỉ gồm chữ, số, ',', '_' hoặc '-'")
    private String areaCode;

    @Schema(description = "Ngày hiệu lực")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hiệu lực")
    private Date expireDatetime;

    @Schema(description = "Người tạo", example = "system")
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Thời điểm tạo")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "system")
    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "Thời điểm cập nhật")
    private Date updateDatetime;

    @Schema(description = "Chu kỳ", example = "1")
    @Min(value = 0, message = "cycle phải >= 0")
    @Max(value = 9999999999L, message = "cycle vượt quá độ dài cột (precision 10)")
    private Long cycle;

    @Schema(description = "Loại danh sách", example = "LIST01")
    @Size(max = 50, message = "listType tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "listType chỉ gồm chữ, số, '_' hoặc '-'")
    private String listType;

    @Schema(description = "Id danh sách con", example = "1")
    @Min(value = 0, message = "subListId phải >= 0")
    @Max(value = 9999999L, message = "subListId vượt quá độ dài cột (precision 7)")
    private Long subListId;

    @Schema(description = "Ghi chú")
    @Size(max = 4000, message = "note tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "note không được chứa ký tự điều khiển")
    private String note;

    @Schema(description = "Id lý do đăng ký", example = "1")
    @Min(value = 0, message = "regReasonId phải >= 0")
    @Max(value = 9999999999L, message = "regReasonId vượt quá độ dài cho phép")
    private Long regReasonId;

    @Schema(description = "Tên lý do", example = "Đấu nối mới")
    @Size(max = 100, message = "reasonName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "reasonName không được chứa ký tự điều khiển")
    private String reasonName;

    @Schema(description = "Mã nhóm phụ", example = "SUBGRP01")
    @Size(max = 50, message = "subGroupCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "subGroupCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String subGroupCode;

    @Schema(description = "Mã sản phẩm", example = "PROD001")
    @Size(max = 50, message = "productCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String productCode;
}
