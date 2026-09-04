package com.viettel.bccs.organization.staff.dto.response;

import java.util.Date;

import com.viettel.bccs.organization.shop.dto.response.ShopResponse;
import com.viettel.bccs.organization.staff.dto.StaffDTO;

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
public class StaffResponse {

    @Schema(description = "ID nhân viên", example = "12345")
    @Min(value = 0, message = "staffId phải >= 0")
    @Max(value = 9999999999L, message = "staffId vượt quá độ dài cột (precision 10)")
    private Long staffId;

    @Schema(description = "Mã nhân viên", example = "NV_001")
    @Size(max = 40, message = "staffCode tối đa 40 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String staffCode;

    @Schema(description = "Tên nhân viên", example = "Nguyễn Văn A")
    @Size(max = 300, message = "name tối đa 300 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Số điện thoại", example = "0909123456")
    @Size(max = 15, message = "tel tối đa 15 ký tự")
    @Pattern(regexp = "^[0-9+\\-\\s]{0,15}$", message = "tel chỉ gồm số, '+', '-' hoặc khoảng trắng")
    private String tel;

    @Schema(description = "Email", example = "nguyenvana@viettel.vn")
    @Size(max = 100, message = "email tối đa 100 ký tự")
    @Pattern(regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "email không đúng định dạng")
    private String email;

    @Schema(description = "Số CMND/CCCD", example = "001234567890")
    @Size(max = 20, message = "idNo tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "idNo chỉ gồm chữ số")
    private String idNo;

    @Schema(description = "Ngày cấp CMND/CCCD", example = "2020-01-01")
    private Date idIssueDate;

    @Schema(description = "Nơi cấp CMND/CCCD", example = "Hà Nội")
    @Size(max = 150, message = "idIssuePlace tối đa 150 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "idIssuePlace không được chứa ký tự điều khiển")
    private String idIssuePlace;

    @Schema(description = "Ngày sinh", example = "1990-01-01")
    private Date birthday;

    @Schema(description = "Địa chỉ", example = "123 Nguyễn Trãi, Quận 1, TP HCM")
    @Size(max = 500, message = "address tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "address không được chứa ký tự điều khiển")
    private String address;

    @Schema(description = "Tỉnh/Thành phố", example = "Hà Nội")
    @Size(max = 50, message = "province tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "province chỉ gồm chữ và số")
    private String province;

    @Schema(description = "Quận/Huyện", example = "Ba Đình")
    @Size(max = 50, message = "district tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "district chỉ gồm chữ và số")
    private String district;

    @Schema(description = "Phường/Xã", example = "Phường 1")
    @Size(max = 50, message = "precinct tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "precinct chỉ gồm chữ và số")
    private String precinct;

    @Schema(description = "ID cửa hàng", example = "12345")
    @Min(value = 0, message = "shopId phải >= 0")
    @Max(value = 9999999999L, message = "shopId vượt quá độ dài cột (precision 10)")
    private Long shopId;

    @Schema(description = "Mã cửa hàng", example = "VTST_HN_001")
    @Size(max = 40, message = "shopCode tối đa 40 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "shopCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopCode;

    @Schema(description = "Tên cửa hàng", example = "Viettel Store Hà Nội")
    @Size(max = 300, message = "shopName tối đa 300 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "shopName không được chứa ký tự điều khiển")
    private String shopName;

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 1, message = "status tối đa 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Tên trạng thái", example = "Đang hoạt động")
    @Size(max = 100, message = "statusName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "statusName không được chứa ký tự điều khiển")
    private String statusName;

    @Schema(description = "ID loại kênh", example = "1")
    @Min(value = 0, message = "channelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
    private Long channelTypeId;

    @Schema(description = "Tên loại kênh", example = "Đại lý")
    @Size(max = 150, message = "channelTypeName tối đa 150 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "channelTypeName không được chứa ký tự điều khiển")
    private String channelTypeName;

    @Schema(description = "Mã loại kênh", example = "CT01")
    @Size(max = 50, message = "channelTypeCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "channelTypeCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String channelTypeCode;

    @Schema(description = "ID shop cha (đơn vị quản lý cấp trên)", example = "10000")
    @Min(value = 0, message = "shopParentId phải >= 0")
    @Max(value = 9999999999L, message = "shopParentId vượt quá độ dài cột (precision 10)")
    private Long shopParentId;

    @Schema(description = "Mã shop cha (đơn vị quản lý cấp trên)", example = "VTST_HN")
    @Size(max = 40, message = "shopParentCode tối đa 40 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "shopParentCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopParentCode;

    @Schema(description = "Tên shop cha (đơn vị quản lý cấp trên)", example = "Viettel Hà Nội")
    @Size(max = 300, message = "shopParentName tối đa 300 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "shopParentName không được chứa ký tự điều khiển")
    private String shopParentName;

    @Schema(description = "Loại nhân viên", example = "1")
    @Min(value = 0, message = "type phải >= 0")
    @Max(value = 9999999999L, message = "type vượt quá độ dài cột (precision 10)")
    private Long type;

    @Schema(description = "ID người dùng", example = "12345")
    @Min(value = 0, message = "userId phải >= 0")
    @Max(value = Long.MAX_VALUE, message = "userId vượt quá độ dài cột (precision 20)")
    private Long userId;

    @Schema(description = "ID chủ sở hữu", example = "12345")
    @Min(value = 0, message = "staffOwnerId phải >= 0")
    @Max(value = 9999999999L, message = "staffOwnerId vượt quá độ dài cột (precision 10)")
    private Long staffOwnerId;

    @Schema(description = "Loại chủ sở hữu", example = "OWNER")
    @Size(max = 1, message = "staffOwnType tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "staffOwnType chỉ gồm chữ hoặc số")
    private String staffOwnType;

    @Schema(description = "Mã vùng", example = "HN")
    @Size(max = 200, message = "areaCode tối đa 200 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,200}$", message = "areaCode chỉ gồm chữ và số")
    private String areaCode;

    @Schema(description = "Thông tin cửa hàng")
    private ShopResponse shop;

    @Schema(description = "Điểm bán hàng (POINT_OF_SALE)", example = "POS01")
    @Size(max = 5, message = "pointOfSale tối đa 5 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,5}$", message = "pointOfSale chỉ gồm chữ, số, '_' hoặc '-'")
    private String pointOfSale;

    public StaffDTO toDTO() {
        StaffDTO dto = new StaffDTO();
        dto.setStaffId(this.staffId);
        dto.setStaffCode(this.staffCode);
        dto.setName(this.name);
        dto.setTel(this.tel);
        dto.setEmail(this.email);
        dto.setIdNo(this.idNo);
        dto.setIdIssuePlace(this.idIssuePlace);
        dto.setIdIssueDate(this.idIssueDate);
        dto.setBirthday(this.birthday);
        dto.setAddress(this.address);
        dto.setProvince(this.province);
        dto.setDistrict(this.district);
        dto.setPrecinct(this.precinct);
        dto.setShopId(this.shopId);
        dto.setStatus(this.status);
        dto.setChannelTypeId(this.channelTypeId);
        dto.setType(this.type);
        dto.setUserId(this.userId);
        dto.setStaffOwnerId(this.staffOwnerId);
        dto.setStaffOwnType(this.staffOwnType);
        dto.setAreaCode(this.areaCode);
        dto.setPointOfSale(this.pointOfSale);
        if (this.shop != null) {
            dto.setShop(this.shop.toDTO());
        }
        return dto;
    }
}
