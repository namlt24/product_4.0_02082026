package com.viettel.bccs.organization.channeltype.dto;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class ChannelTypeDTO {

    @Schema(description = "ID loại kênh", example = "1")
    @Min(value = 0, message = "channelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
    private Long channelTypeId;

    @Schema(description = "Tên loại kênh", example = "Đại lý")
    @Size(max = 50, message = "name tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Trạng thái: 0 Không hiệu lực, 1 Hiệu lực", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Loại đối tượng: 1 shop, 2 staff", example = "1")
    @Size(max = 1, message = "objectType tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,1}$", message = "objectType chỉ gồm chữ, số, '_' hoặc '-'")
    private String objectType;

    @Schema(description = "Thuộc Viettel: 1 Thuộc VT, 2 Không thuộc Viettel", example = "1")
    @Size(max = 1, message = "isVtUnit tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,1}$", message = "isVtUnit chỉ gồm chữ, số, '_' hoặc '-'")
    private String isVtUnit;

    @Schema(description = "Tính hoa hồng: 1 Tính, 2 Không tính", example = "1")
    @Size(max = 1, message = "checkComm tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,1}$", message = "checkComm chỉ gồm chữ, số, '_' hoặc '-'")
    private String checkComm;

    @Schema(description = "Loại kho thao tác: 1 shop, 2 staff", example = "1")
    @Min(value = 0, message = "stockType phải >= 0")
    @Max(value = 9, message = "stockType vượt quá độ dài cột (precision 1)")
    private Integer stockType;

    @Schema(description = "Mẫu phiếu xuất nhập kho", example = "TEMPLATE_001")
    @Size(max = 50, message = "stockReportTemplate tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "stockReportTemplate không được chứa ký tự điều khiển")
    private String stockReportTemplate;

    @Schema(description = "Số lần đặt cọc tối đa", example = "10")
    @Min(value = 0, message = "totalDebit phải >= 0")
    @Max(value = 9999999999L, message = "totalDebit vượt quá độ dài cột (precision 10)")
    private Long totalDebit;

    @Schema(description = "Cho phép thêm batch", example = "1")
    @Min(value = 0, message = "allowAddBatch phải >= 0")
    @Max(value = 9, message = "allowAddBatch vượt quá độ dài cột (precision 1)")
    private Integer allowAddBatch;

    @Schema(description = "Mã đối tượng suffix", example = "VTST")
    @Size(max = 50, message = "suffixObjectCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "suffixObjectCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String suffixObjectCode;

    @Schema(description = "Role cập nhật staff owner", example = "ROLE_ADMIN")
    @Size(max = 100, message = "updateStaffOwnerRole tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "updateStaffOwnerRole chỉ gồm chữ, số, '_' hoặc '-'")
    private String updateStaffOwnerRole;

    @Schema(description = "Mã chính sách giảm giá mặc định", example = "DISC_DEFAULT")
    @Size(max = 100, message = "discountPolicyDefaut tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "discountPolicyDefaut chỉ gồm chữ, số, '_' hoặc '-'")
    private String discountPolicyDefaut;

    @Schema(description = "Mã chính sách giá mặc định", example = "PRICE_DEFAULT")
    @Size(max = 100, message = "pricePolicyDefaut tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "pricePolicyDefaut chỉ gồm chữ, số, '_' hoặc '-'")
    private String pricePolicyDefaut;

    @Schema(description = "Role cập nhật mã trắng", example = "ROLE_BLANK")
    @Size(max = 100, message = "updateBlankCodeRole tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "updateBlankCodeRole chỉ gồm chữ, số, '_' hoặc '-'")
    private String updateBlankCodeRole;

    @Schema(description = "Role cập nhật thông tin đối tượng", example = "ROLE_OBJ_INFO")
    @Size(max = 100, message = "updateObjectInfoRole tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "updateObjectInfoRole chỉ gồm chữ, số, '_' hoặc '-'")
    private String updateObjectInfoRole;

    @Schema(description = "Role cập nhật cửa hàng", example = "ROLE_SHOP")
    @Size(max = 100, message = "updateShopRole tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "updateShopRole chỉ gồm chữ, số, '_' hoặc '-'")
    private String updateShopRole;

    @Schema(description = "Mã loại kênh", example = "CT01")
    @Size(max = 50, message = "code tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
    private String code;

    @Schema(description = "Mã nhóm loại kênh", example = "5")
    @Min(value = 0, message = "groupChannelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "groupChannelTypeId vượt quá độ dài cột (precision 10)")
    private Long groupChannelTypeId;

    @Schema(description = "Mã nhóm kênh", example = "10")
    @Min(value = 0, message = "groupChannelId phải >= 0")
    @Max(value = 9999999999L, message = "groupChannelId vượt quá độ dài cột (precision 10)")
    private Long groupChannelId;

    @Schema(description = "Trường xác định kênh VHR: 1 Lấy từ VHR", example = "0")
    @Min(value = 0, message = "isVhrChannel phải >= 0")
    @Max(value = 99, message = "isVhrChannel vượt quá độ dài cột (precision 2)")
    private Integer isVhrChannel;

    @Schema(description = "Kênh thu hộ", example = "1")
    @Min(value = 0, message = "isCollChannel phải >= 0")
    @Max(value = 99, message = "isCollChannel vượt quá độ dài cột (precision 2)")
    private Integer isCollChannel;

    @Schema(description = "Không tạo mã trắng", example = "1")
    @Min(value = 0, message = "isNotBlankCode phải >= 0")
    @Max(value = 99, message = "isNotBlankCode vượt quá độ dài cột (precision 2)")
    private Integer isNotBlankCode;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDatetime;

    @Schema(description = "Người tạo", example = "admin")
    @Size(max = 100, message = "createUser tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,100}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Người cập nhật", example = "admin")
    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "Ngày cập nhật", example = "2024-06-01")
    private Date updateDatetime;

    @Schema(description = "Mã thanh toán", example = "PAY001")
    @Size(max = 100, message = "paymentCode tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "paymentCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String paymentCode;

    @Schema(description = "Hậu tố thanh toán", example = "TAIL")
    @Size(max = 100, message = "paymentTail tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "paymentTail chỉ gồm chữ, số, '_' hoặc '-'")
    private String paymentTail;

    @Schema(description = "Giao khách hàng", example = "1")
    @Min(value = 0, message = "assignCustStatus phải >= 0")
    @Max(value = 9, message = "assignCustStatus vượt quá độ dài cột (precision 1)")
    private Integer assignCustStatus;

    @Schema(description = "Mô tả", example = "Kênh bán hàng Viettel Store")
    @Size(max = 1000, message = "description tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "description không được chứa ký tự điều khiển")
    private String description;
}
