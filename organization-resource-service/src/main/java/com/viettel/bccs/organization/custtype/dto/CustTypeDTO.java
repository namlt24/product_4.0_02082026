package com.viettel.bccs.organization.custtype.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class CustTypeDTO {

    @Schema(description = "Mã loại khách hàng", example = "PREPAID")
    @Size(min = 1, max = 6, message = "custType tối đa 6 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{1,6}$", message = "custType chỉ gồm chữ, số, '_' hoặc '-'")
    private String custType;

    @Schema(description = "Tên loại khách hàng", example = "Trả trước")
    @Size(max = 100, message = "name tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Người tạo", example = "admin")
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "admin")
    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "Ngày cập nhật", example = "2024-06-01")
    private Date updateDatetime;

    @Schema(description = "Mô tả", example = "Khách hàng trả trước")
    @Size(max = 512, message = "description tối đa 512 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Schema(description = "Trạng thái", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Loại khách hàng: 1 Cá nhân trong nước, 2 Doanh nghiệp, 3 Nước ngoài", example = "1")
    @Size(max = 1, message = "groupType tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,1}$", message = "groupType chỉ gồm chữ, số, '_' hoặc '-'")
    private String groupType;

    @Schema(description = "Thuế", example = "10")
    @Min(value = 0, message = "tax phải >= 0")
    @Max(value = 9999999999L, message = "tax vượt quá độ dài cột (precision 10)")
    private Long tax;

    @Schema(description = "Quy hoạch: 0 Khách hàng cũ, 1 Khách hàng mới", example = "1")
    @Size(min = 1, max = 1, message = "plan đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "plan chỉ nhận giá trị 0 hoặc 1")
    private String plan;

    @Schema(description = "Có khách hàng đại diện: 1 Có, 0 Không", example = "0")
    @Size(min = 1, max = 1, message = "representCust đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "representCust chỉ nhận giá trị 0 hoặc 1")
    private String representCust;

    @Schema(description = "ID loại khách hàng", example = "1")
    @Min(value = 0, message = "custTypeId phải >= 0")
    @Max(value = 9999999999L, message = "custTypeId vượt quá độ dài cột (precision 10)")
    private Long custTypeId;
}
