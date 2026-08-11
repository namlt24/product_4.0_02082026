package com.viettel.bccs.policy.client.dto;

import com.viettel.bccs.policy.mapactiveinfo.dto.response.ShopResponse;
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
public class StaffResponse {

    @Schema(description = "ID nhân viên", example = "12345")
    private Long staffId;

    @Schema(description = "Mã nhân viên", example = "NV_001")
    private String staffCode;

    @Schema(description = "Tên nhân viên", example = "Nguyễn Văn A")
    private String name;

    @Schema(description = "Số điện thoại", example = "0909123456")
    private String tel;

    @Schema(description = "Email", example = "nguyenvana@viettel.vn")
    private String email;

    @Schema(description = "Số CMND/CCCD", example = "001234567890")
    private String idNo;

    @Schema(description = "Ngày cấp CMND/CCCD", example = "2020-01-01")
    private Date idIssueDate;

    @Schema(description = "Nơi cấp CMND/CCCD", example = "Hà Nội")
    private String idIssuePlace;

    @Schema(description = "Ngày sinh", example = "1990-01-01")
    private Date birthday;

    @Schema(description = "Địa chỉ", example = "123 Nguyễn Trãi, Quận 1, TP HCM")
    private String address;

    @Schema(description = "Tỉnh/Thành phố", example = "Hà Nội")
    private String province;

    @Schema(description = "Quận/Huyện", example = "Ba Đình")
    private String district;

    @Schema(description = "Phường/Xã", example = "Phường 1")
    private String precinct;

    @Schema(description = "ID cửa hàng", example = "12345")
    private Long shopId;

    @Schema(description = "Mã cửa hàng", example = "VTST_HN_001")
    private String shopCode;

    @Schema(description = "Tên cửa hàng", example = "Viettel Store Hà Nội")
    private String shopName;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Tên trạng thái", example = "Đang hoạt động")
    private String statusName;

    @Schema(description = "ID loại kênh", example = "1")
    private Long channelTypeId;

    @Schema(description = "Tên loại kênh", example = "Đại lý")
    private String channelTypeName;

    @Schema(description = "Loại nhân viên", example = "1")
    private Long type;

    @Schema(description = "ID người dùng", example = "12345")
    private Long userId;

    @Schema(description = "ID chủ sở hữu", example = "12345")
    private Long staffOwnerId;

    @Schema(description = "Loại chủ sở hữu", example = "OWNER")
    private String staffOwnType;

    @Schema(description = "Mã vùng", example = "HN")
    private String areaCode;

    @Schema(description = "Thông tin cửa hàng")
    private ShopResponse shop;

    @Schema(description = "Điểm bán hàng (POINT_OF_SALE)", example = "POS01")
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
        dto.setChannelTypeId(this.channelTypeId);
        dto.setType(this.type);
        dto.setUserId(this.userId);
        dto.setStaffOwnerId(this.staffOwnerId);
        dto.setStaffOwnType(this.staffOwnType);
        dto.setAreaCode(this.areaCode);
        dto.setShopCode(this.shopCode);
        dto.setPointOfSale(this.pointOfSale);
        return dto;
    }
}