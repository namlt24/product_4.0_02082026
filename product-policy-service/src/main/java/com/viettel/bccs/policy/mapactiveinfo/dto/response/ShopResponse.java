package com.viettel.bccs.policy.mapactiveinfo.dto.response;

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

}
