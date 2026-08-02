package com.viettel.bccs.organization.shop.dto.response;

import com.viettel.bccs.organization.shop.dto.ShopDTO;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class ShopResponse {

    @Schema(description = "ID cửa hàng", example = "12345")
    private Long shopId;

    @Schema(description = "Tên cửa hàng", example = "Viettel Store Hà Nội")
    private String name;

    @Schema(description = "Mã cửa hàng", example = "VTST_HN_001")
    private String shopCode;

    @Schema(description = "ID cửa hàng cha", example = "10000")
    private Long parentShopId;

    @Schema(description = "Địa chỉ cửa hàng", example = "123 Nguyễn Trãi, Quận 1, TP HCM")
    private String address;

    @Schema(description = "Số điện thoại", example = "0909123456")
    private String tel;

    @Schema(description = "Email", example = "contact@viettel.vn")
    private String email;

    @Schema(description = "Tỉnh/Thành phố", example = "Hà Nội")
    private String province;

    @Schema(description = "Quận/Huyện", example = "Ba Đình")
    private String district;

    @Schema(description = "Phường/Xã", example = "Phường 1")
    private String precinct;

    @Schema(description = "ID loại kênh", example = "1")
    private Long channelTypeId;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Đường dẫn cửa hàng", example = "/HN/VTST_HN_001")
    private String shopPath;

    @Schema(description = "Loại cửa hàng", example = "1")
    private String shopType;

    @Schema(description = "Mã vùng", example = "HN")
    private String areaCode;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDatetime;

    @Schema(description = "ID nhóm loại kênh", example = "5")
    private Long groupChannelTypeId;

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
        return dto;
    }
}