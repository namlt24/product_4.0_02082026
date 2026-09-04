package com.viettel.bccs.organization.shop.dto.response;

import java.util.Date;

import com.viettel.bccs.organization.shop.dto.ShopDTO;

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
public class ShopResponse {

    @Schema(description = "ID cửa hàng", example = "12345")
    @Min(value = 0, message = "shopId phải >= 0")
    @Max(value = 9999999999L, message = "shopId vượt quá độ dài cột (precision 10)")
    private Long shopId;

    @Schema(description = "Tên cửa hàng", example = "Viettel Store Hà Nội")
    @Size(max = 300, message = "name tối đa 300 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Mã cửa hàng", example = "VTST_HN_001")
    @Size(max = 40, message = "shopCode tối đa 40 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "shopCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopCode;

    @Schema(description = "ID cửa hàng cha", example = "10000")
    @Min(value = 0, message = "parentShopId phải >= 0")
    @Max(value = 9999999999L, message = "parentShopId vượt quá độ dài cột (precision 10)")
    private Long parentShopId;

    @Schema(description = "Địa chỉ cửa hàng", example = "123 Nguyễn Trãi, Quận 1, TP HCM")
    @Size(max = 500, message = "address tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "address không được chứa ký tự điều khiển")
    private String address;

    @Schema(description = "Số điện thoại", example = "0909123456")
    @Size(max = 100, message = "tel tối đa 100 ký tự")
    @Pattern(regexp = "^[0-9+\\-\\s]{0,100}$", message = "tel chỉ gồm số, '+', '-' hoặc khoảng trắng")
    private String tel;

    @Schema(description = "Email", example = "contact@viettel.vn")
    @Size(max = 200, message = "email tối đa 200 ký tự")
    @Pattern(regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "email không đúng định dạng")
    private String email;

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

    @Schema(description = "ID loại kênh", example = "1")
    @Min(value = 0, message = "channelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
    private Long channelTypeId;

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 1, message = "status tối đa 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Đường dẫn cửa hàng", example = "/HN/VTST_HN_001")
    @Size(max = 500, message = "shopPath tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "shopPath không được chứa ký tự điều khiển")
    private String shopPath;

    @Schema(description = "Loại cửa hàng", example = "1")
    @Size(max = 1, message = "shopType tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "shopType chỉ gồm chữ hoặc số")
    private String shopType;

    @Schema(description = "Mã vùng", example = "HN")
    @Size(max = 200, message = "areaCode tối đa 200 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,200}$", message = "areaCode chỉ gồm chữ và số")
    private String areaCode;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDatetime;

    @Schema(description = "ID nhóm loại kênh", example = "5")
    @Min(value = 0, message = "groupChannelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "groupChannelTypeId vượt quá độ dài cột (precision 10)")
    private Long groupChannelTypeId;

    @Schema(description = "ID nhân viên quản lý cửa hàng", example = "100")
    @Min(value = 0, message = "staffOwnerId phải >= 0")
    @Max(value = 9999999999L, message = "staffOwnerId vượt quá độ dài cột (precision 10)")
    private Long staffOwnerId;

    public ShopDTO toDTO() {
        ShopDTO dto = new ShopDTO();
        dto.setShopId(this.shopId);
        dto.setName(this.name);
        dto.setShopCode(this.shopCode);
        dto.setParentShopId(this.parentShopId);
        dto.setAddress(this.address);
        dto.setTel(this.tel);
        dto.setEmail(this.email);
        dto.setProvince(this.province);
        dto.setDistrict(this.district);
        dto.setPrecinct(this.precinct);
        dto.setChannelTypeId(this.channelTypeId);
        dto.setStatus(this.status);
        dto.setShopPath(this.shopPath);
        dto.setShopType(this.shopType);
        dto.setAreaCode(this.areaCode);
        dto.setCreateDatetime(this.createDatetime);
        dto.setGroupChannelTypeId(this.groupChannelTypeId);
        dto.setStaffOwnerId(this.staffOwnerId);
        return dto;
    }
}
