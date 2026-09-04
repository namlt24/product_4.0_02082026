package com.viettel.bccs.productcatalog.client.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Thông tin cửa hàng (cross-service DTO)")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ShopDTO implements Serializable {

    @Schema(description = "ID cửa hàng", example = "12345")
    @JsonProperty(value = "shopId")
    private Long shopId;

    @Schema(description = "Tên cửa hàng", example = "Viettel Store Hà Nội")
    @JsonProperty(value = "name")
    private String name;

    @Schema(description = "ID cửa hàng cha", example = "10000")
    @JsonProperty(value = "parentShopId")
    private Long parentShopId;

    @Schema(description = "Mã cửa hàng", example = "VTST_HN_001")
    @JsonProperty(value = "shopCode")
    private String shopCode;

    @Schema(description = "Loại cửa hàng", example = "1")
    @JsonProperty(value = "shopType")
    private String shopType;

    @Schema(description = "Trạng thái", example = "1")
    @JsonProperty(value = "status")
    private String status;

    @Schema(description = "Địa chỉ", example = "123 Nguyễn Trãi, Quận 1, TP HCM")
    @JsonProperty(value = "address")
    private String address;

    @Schema(description = "Tỉnh/Thành phố", example = "Hà Nội")
    @JsonProperty(value = "province")
    private String province;

    @Schema(description = "Quận/Huyện", example = "Ba Đình")
    @JsonProperty(value = "district")
    private String district;

    @Schema(description = "Phường/Xã", example = "Phường 1")
    @JsonProperty(value = "precinct")
    private String precinct;

    @Schema(description = "Số điện thoại", example = "0909123456")
    @JsonProperty(value = "tel")
    private String tel;

    @Schema(description = "Email", example = "contact@viettel.vn")
    @JsonProperty(value = "email")
    private String email;

    @Schema(description = "Mã cửa hàng cha", example = "VTST_HN")
    @JsonProperty(value = "parShopCode")
    private String parShopCode;

    @Schema(description = "Đường dẫn cửa hàng", example = "/HN/VTST_HN_001")
    @JsonProperty(value = "shopPath")
    private String shopPath;

    @Schema(description = "Tên file đính kèm", example = "hop_dong.pdf")
    @JsonProperty(value = "fileName")
    private String fileName;

    @Schema(description = "Giấy phép kinh doanh", example = "GPKD_001")
    @JsonProperty(value = "businessLicence")
    private String businessLicence;

    @Schema(description = "ID loại kênh", example = "1")
    @JsonProperty(value = "channelTypeId")
    private Long channelTypeId;

    @Schema(description = "ID nhóm loại kênh", example = "5")
    @JsonProperty(value = "groupChannelTypeId")
    private Long groupChannelTypeId;

    @Schema(description = "ID người quản lý cửa hàng", example = "999")
    @JsonProperty(value = "shopKeeperId")
    private Long shopKeeperId;

    @Schema(description = "ID giám đốc cửa hàng", example = "888")
    @JsonProperty(value = "shopDirectorId")
    private Long shopDirectorId;

    @Schema(description = "ID tenant", example = "1")
    @JsonProperty(value = "tenantId")
    private Long tenantId;

    @Schema(description = "Người tạo", example = "admin")
    @JsonProperty(value = "createUser")
    private String createUser;

    @Schema(description = "Người cập nhật", example = "admin")
    @JsonProperty(value = "updateUser")
    private String updateUser;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    @JsonProperty(value = "createDatetime")
    private Date createDatetime;

    @Schema(description = "Ngày cập nhật", example = "2024-06-01")
    @JsonProperty(value = "updateDateTime")
    private Date updateDateTime;
}
