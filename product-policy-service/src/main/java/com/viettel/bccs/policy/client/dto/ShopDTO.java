package com.viettel.bccs.policy.client.dto;

import java.io.Serializable;
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Schema
@Getter
@Setter
public class ShopDTO implements Serializable {

    @Schema(description = "ID cửa hàng", example = "12345")
    private Long shopId;

    @Schema(description = "Tên cửa hàng", example = "Viettel Store Hà Nội")
    private String name;

    @Schema(description = "ID cửa hàng cha", example = "10000")
    private Long parentShopId;

    @Schema(description = "Tài khoản ngân hàng", example = "1234567890")
    private String account;

    @Schema(description = "Tên ngân hàng", example = "Vietcombank")
    private String bankName;

    @Schema(description = "Địa chỉ cửa hàng", example = "123 Nguyễn Trãi, Quận 1, TP HCM")
    private String address;

    @Schema(description = "Số điện thoại", example = "0909123456")
    private String tel;

    @Schema(description = "Số fax", example = "02812345678")
    private String fax;

    @Schema(description = "Mã cửa hàng", example = "VTST_HN_001")
    private String shopCode;

    @Schema(description = "Loại cửa hàng", example = "1")
    private String shopType;

    @Schema(description = "Tên người liên hệ", example = "Nguyễn Văn A")
    private String contactName;

    @Schema(description = "Chức danh người liên hệ", example = "Giám đốc")
    private String contactTitle;

    @Schema(description = "Số điện thoại liên hệ", example = "0909123456")
    private String telNumber;

    @Schema(description = "Email", example = "contact@viettel.vn")
    private String email;

    @Schema(description = "Mô tả", example = "Cửa hàng Viettel Store")
    private String description;

    @Schema(description = "Tỉnh/Thành phố", example = "Hà Nội")
    private String province;

    @Schema(description = "Mã cửa hàng cha", example = "VTST_HN")
    private String parShopCode;

    @Schema(description = "Mã trung tâm", example = "1")
    private String centerCode;

    @Schema(description = "Mã cũ của cửa hàng", example = "OLD123")
    private String oldShopCode;

    @Schema(description = "Thông tin công ty", example = "Viettel Group")
    private String company;

    @Schema(description = "Mã số thuế", example = "0123456789")
    private String tin;

    @Schema(description = "Cửa hàng", example = "Viettel")
    private String shop;

    @Schema(description = "Mã tỉnh", example = "HN")
    private String provinceCode;

    @Schema(description = "Thanh toán hoa hồng", example = "Y")
    private String payComm;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDate;

    @Schema(description = "ID loại kênh", example = "1")
    private Long channelTypeId;

    @Schema(description = "Chính sách giảm giá", example = "DISC_001")
    private String discountPolicy;

    @Schema(description = "Chính sách giá", example = "PRICE_001")
    private String pricePolicy;

    @Schema(description = "Đường dẫn cửa hàng", example = "/HN/VTST_HN_001")
    private String shopPath;

    @Schema(description = "Quận/Huyện", example = "Ba Đình")
    private String district;

    @Schema(description = "Phường/Xã", example = "Phường 1")
    private String precinct;

    @Schema(description = "Mã vùng", example = "HN")
    private String areaCode;

    @Schema(description = "Số CMND/CCCD", example = "001234567890")
    private String idNo;

    @Schema(description = "Nơi cấp CMND/CCCD", example = "Hà Nội")
    private String idIssuePlace;

    @Schema(description = "Ngày cấp CMND/CCCD", example = "2020-01-01")
    private Date idIssueDate;

    @Schema(description = "Khối đường", example = "Phường 1")
    private String streetBlock;

    @Schema(description = "Đường", example = "Nguyễn Trãi")
    private String street;

    @Schema(description = "Số nhà", example = "123")
    private String home;

    @Schema(description = "Đường dẫn tên cửa hàng", example = "Hà Nội > Ba Đình > Viettel Store")
    private String shopPathName;

    @Schema(description = "Số hợp đồng", example = "HD_2024_001")
    private String contractNo;

    @Schema(description = "Tên file đính kèm", example = "hop_dong.pdf")
    private String fileName;

    @Schema(description = "Giấy phép kinh doanh", example = "GPKD_001")
    private String businessLicence;

    @Schema(description = "Số bankplus", example = "0909123456")
    private String bankplusMobile;

    @Schema(description = "Số kho nhập", example = "100")
    private Integer stockNum;

    @Schema(description = "Số kho xuất", example = "50")
    private Integer stockNumImp;

    @Schema(description = "Ngày cập nhật", example = "2024-06-01")
    private Date updateDateTime;

    @Schema(description = "Mã ngân hàng", example = "VCB")
    private String bankCode;

    @Schema(description = "ID người quản lý cửa hàng", example = "999")
    private Long shopKeeperId;

    @Schema(description = "ID giám đốc cửa hàng", example = "888")
    private Long shopDirectorId;

    @Schema(description = "ID nhóm loại kênh", example = "5")
    private Long groupChannelTypeId;

    @Schema(description = "ID chủ sở hữu nhân viên", example = "100")
    private Long staffOwnerId;

    @Schema(description = "ID tenant", example = "1")
    private Long tenantId;

    @Schema(description = "Tỉnh kinh doanh", example = "HN")
    private String businessProvince;

    @Schema(description = "Quận kinh doanh", example = "Ba Đình")
    private String businessDistrict;

    @Schema(description = "Phường kinh doanh", example = "Phường 1")
    private String businessPrecinct;

    @Schema(description = "Khối đường kinh doanh", example = "Phường 1")
    private String businessStreetBlock;

    @Schema(description = "Đường kinh doanh", example = "Nguyễn Trãi")
    private String businessStreet;

    @Schema(description = "Số nhà kinh doanh", example = "456")
    private String businessHome;

    @Schema(description = "Mã vùng kinh doanh", example = "HN")
    private String businessAreacode;

    @Schema(description = "Địa chỉ kinh doanh", example = "456 Nguyễn Trãi, Ba Đình, Hà Nội")
    private String businessAddress;

    @Schema(description = "Người tạo", example = "admin")
    private String createUser;

    @Schema(description = "Người cập nhật", example = "admin")
    private String updateUser;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDatetime;

    @Schema(description = "Trạng thái", example = "1")
    private Integer status;

    @Schema(description = "Doanh thu", example = "Y")
    private String turnover;

    @Schema(description = "Ngày sinh", example = "1990-01-01")
    private Date birthday;

    // --- Getters ---


    // --- Setters ---

}