package com.viettel.bccs.organization.shop.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài/precision cột thật của SHOP (xem ShopEntity).
 * Field không nullable ở DB vẫn cho phép null ở DTO, @Size/@Pattern/@Min/@Max chỉ áp dụng khi giá trị khác null.
 */
@Schema
public class ShopDTO {

    @Schema(description = "ID cửa hàng", example = "12345")
    @Min(value = 0, message = "shopId phải >= 0")
    @Max(value = 9999999999L, message = "shopId vượt quá độ dài cột (precision 10)")
    private Long shopId;

    @Schema(description = "Tên cửa hàng", example = "Viettel Store Hà Nội")
    @Size(max = 300, message = "name tối đa 300 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "ID cửa hàng cha", example = "10000")
    @Min(value = 0, message = "parentShopId phải >= 0")
    @Max(value = 9999999999L, message = "parentShopId vượt quá độ dài cột (precision 10)")
    private Long parentShopId;

    @Schema(description = "Tài khoản ngân hàng", example = "1234567890")
    @Size(max = 40, message = "account tối đa 40 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,40}$", message = "account không được chứa ký tự điều khiển")
    private String account;

    @Schema(description = "Tên ngân hàng", example = "Vietcombank")
    @Size(max = 150, message = "bankName tối đa 150 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "bankName không được chứa ký tự điều khiển")
    private String bankName;

    @Schema(description = "Địa chỉ cửa hàng", example = "123 Nguyễn Trãi, Quận 1, TP HCM")
    @Size(max = 500, message = "address tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "address không được chứa ký tự điều khiển")
    private String address;

    @Schema(description = "Số điện thoại", example = "0909123456")
    @Size(max = 100, message = "tel tối đa 100 ký tự")
    @Pattern(regexp = "^[0-9+\\-\\s]{0,100}$", message = "tel chỉ gồm số, '+', '-' hoặc khoảng trắng")
    private String tel;

    @Schema(description = "Số fax", example = "02812345678")
    @Size(max = 100, message = "fax tối đa 100 ký tự")
    @Pattern(regexp = "^[0-9+\\-\\s]{0,100}$", message = "fax chỉ gồm số, '+', '-' hoặc khoảng trắng")
    private String fax;

    @Schema(description = "Mã cửa hàng", example = "VTST_HN_001")
    @Size(max = 40, message = "shopCode tối đa 40 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "shopCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopCode;

    @Schema(description = "Loại cửa hàng", example = "1")
    @Size(max = 1, message = "shopType tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "shopType chỉ gồm chữ hoặc số")
    private String shopType;

    @Schema(description = "Tên người liên hệ", example = "Nguyễn Văn A")
    @Size(max = 100, message = "contactName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "contactName không được chứa ký tự điều khiển")
    private String contactName;

    @Schema(description = "Chức danh người liên hệ", example = "Giám đốc")
    @Size(max = 30, message = "contactTitle tối đa 30 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,30}$", message = "contactTitle không được chứa ký tự điều khiển")
    private String contactTitle;

    @Schema(description = "Số điện thoại liên hệ", example = "0909123456")
    @Size(max = 30, message = "telNumber tối đa 30 ký tự")
    @Pattern(regexp = "^[0-9+\\-\\s]{0,30}$", message = "telNumber chỉ gồm số, '+', '-' hoặc khoảng trắng")
    private String telNumber;

    @Schema(description = "Email", example = "contact@viettel.vn")
    @Size(max = 200, message = "email tối đa 200 ký tự")
    @Pattern(regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "email không đúng định dạng")
    private String email;

    @Schema(description = "Mô tả", example = "Cửa hàng Viettel Store")
    @Size(max = 100, message = "description tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Schema(description = "Tỉnh/Thành phố", example = "Hà Nội")
    @Size(max = 50, message = "province tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "province chỉ gồm chữ và số")
    private String province;

    @Schema(description = "Mã cửa hàng cha", example = "VTST_HN")
    @Size(max = 20, message = "parShopCode tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "parShopCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String parShopCode;

    @Schema(description = "Mã trung tâm", example = "1")
    @Size(max = 1, message = "centerCode tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "centerCode chỉ gồm chữ hoặc số")
    private String centerCode;

    @Schema(description = "Mã cũ của cửa hàng", example = "OLD123")
    @Size(max = 40, message = "oldShopCode tối đa 40 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "oldShopCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String oldShopCode;

    @Schema(description = "Thông tin công ty", example = "Viettel Group")
    @Size(max = 4000, message = "company tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "company không được chứa ký tự điều khiển")
    private String company;

    @Schema(description = "Mã số thuế", example = "0123456789")
    @Size(max = 50, message = "tin tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "tin chỉ gồm chữ, số, '_' hoặc '-'")
    private String tin;

    @Schema(description = "Cửa hàng", example = "Viettel")
    @Size(max = 20, message = "shop tối đa 20 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,20}$", message = "shop không được chứa ký tự điều khiển")
    private String shop;

    @Schema(description = "Mã tỉnh", example = "HN")
    @Size(max = 50, message = "provinceCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "provinceCode chỉ gồm chữ và số")
    private String provinceCode;

    @Schema(description = "Thanh toán hoa hồng", example = "Y")
    @Size(max = 1, message = "payComm tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "payComm chỉ gồm chữ hoặc số")
    private String payComm;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDate;

    @Schema(description = "ID loại kênh", example = "1")
    @Min(value = 0, message = "channelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
    private Long channelTypeId;

    @Schema(description = "Chính sách giảm giá", example = "DISC_001")
    @Size(max = 20, message = "discountPolicy tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "discountPolicy chỉ gồm chữ, số, '_' hoặc '-'")
    private String discountPolicy;

    @Schema(description = "Chính sách giá", example = "PRICE_001")
    @Size(max = 20, message = "pricePolicy tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "pricePolicy chỉ gồm chữ, số, '_' hoặc '-'")
    private String pricePolicy;

    @Schema(description = "Đường dẫn cửa hàng", example = "/HN/VTST_HN_001")
    @Size(max = 500, message = "shopPath tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "shopPath không được chứa ký tự điều khiển")
    private String shopPath;

    @Schema(description = "Quận/Huyện", example = "Ba Đình")
    @Size(max = 50, message = "district tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "district chỉ gồm chữ và số")
    private String district;

    @Schema(description = "Phường/Xã", example = "Phường 1")
    @Size(max = 50, message = "precinct tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "precinct chỉ gồm chữ và số")
    private String precinct;

    @Schema(description = "Mã vùng", example = "HN")
    @Size(max = 200, message = "areaCode tối đa 200 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,200}$", message = "areaCode chỉ gồm chữ và số")
    private String areaCode;

    @Schema(description = "Số CMND/CCCD", example = "001234567890")
    @Size(max = 50, message = "idNo tối đa 50 ký tự")
    @Pattern(regexp = "^[0-9]{0,50}$", message = "idNo chỉ gồm chữ số")
    private String idNo;

    @Schema(description = "Nơi cấp CMND/CCCD", example = "Hà Nội")
    @Size(max = 100, message = "idIssuePlace tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "idIssuePlace không được chứa ký tự điều khiển")
    private String idIssuePlace;

    @Schema(description = "Ngày cấp CMND/CCCD", example = "2020-01-01")
    private Date idIssueDate;

    @Schema(description = "Khối đường", example = "Phường 1")
    @Size(max = 100, message = "streetBlock tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "streetBlock không được chứa ký tự điều khiển")
    private String streetBlock;

    @Schema(description = "Đường", example = "Nguyễn Trãi")
    @Size(max = 100, message = "street tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "street không được chứa ký tự điều khiển")
    private String street;

    @Schema(description = "Số nhà", example = "123")
    @Size(max = 100, message = "home tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "home không được chứa ký tự điều khiển")
    private String home;

    @Schema(description = "Loại giấy tờ", example = "1")
    @Min(value = 0, message = "idType phải >= 0")
    @Max(value = 99, message = "idType vượt quá độ dài cột (precision 2)")
    private Integer idType;

    @Schema(description = "Đường dẫn tên cửa hàng", example = "Hà Nội > Ba Đình > Viettel Store")
    @Size(max = 2000, message = "shopPathName tối đa 2000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,2000}$", message = "shopPathName không được chứa ký tự điều khiển")
    private String shopPathName;

    @Schema(description = "Số hợp đồng", example = "HD_2024_001")
    @Size(max = 100, message = "contractNo tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "contractNo chỉ gồm chữ, số, '_' hoặc '-'")
    private String contractNo;

    @Schema(description = "Tên file đính kèm", example = "hop_dong.pdf")
    @Size(max = 200, message = "fileName tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "fileName không được chứa ký tự điều khiển")
    private String fileName;

    @Schema(description = "Giấy phép kinh doanh", example = "GPKD_001")
    @Size(max = 200, message = "businessLicence tối đa 200 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,200}$", message = "businessLicence chỉ gồm chữ, số, '_' hoặc '-'")
    private String businessLicence;

    @Schema(description = "Số bankplus", example = "0909123456")
    @Size(max = 20, message = "bankplusMobile tối đa 20 ký tự")
    @Pattern(regexp = "^[0-9+\\-\\s]{0,20}$", message = "bankplusMobile chỉ gồm số, '+', '-' hoặc khoảng trắng")
    private String bankplusMobile;

    @Schema(description = "Số kho nhập", example = "100")
    @Min(value = 0, message = "stockNum phải >= 0")
    @Max(value = 9999999, message = "stockNum vượt quá độ dài cột (precision 7)")
    private Integer stockNum;

    @Schema(description = "Số kho xuất", example = "50")
    @Min(value = 0, message = "stockNumImp phải >= 0")
    @Max(value = 9999999, message = "stockNumImp vượt quá độ dài cột (precision 7)")
    private Integer stockNumImp;

    @Schema(description = "Ngày cập nhật", example = "2024-06-01")
    private Date updateDateTime;

    @Schema(description = "Mã ngân hàng", example = "VCB")
    @Size(max = 30, message = "bankCode tối đa 30 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,30}$", message = "bankCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String bankCode;

    @Schema(description = "ID người quản lý cửa hàng", example = "999")
    @Min(value = 0, message = "shopKeeperId phải >= 0")
    @Max(value = Long.MAX_VALUE, message = "shopKeeperId vượt quá độ dài cột (precision 20)")
    private Long shopKeeperId;

    @Schema(description = "ID giám đốc cửa hàng", example = "888")
    @Min(value = 0, message = "shopDirectorId phải >= 0")
    @Max(value = Long.MAX_VALUE, message = "shopDirectorId vượt quá độ dài cột (precision 20)")
    private Long shopDirectorId;

    @Schema(description = "ID nhóm loại kênh", example = "5")
    @Min(value = 0, message = "groupChannelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "groupChannelTypeId vượt quá độ dài cột (precision 10)")
    private Long groupChannelTypeId;

    @Schema(description = "ID chủ sở hữu nhân viên", example = "100")
    @Min(value = 0, message = "staffOwnerId phải >= 0")
    @Max(value = 9999999999L, message = "staffOwnerId vượt quá độ dài cột (precision 10)")
    private Long staffOwnerId;

    @Schema(description = "ID tenant", example = "1")
    @Min(value = 0, message = "tenantId phải >= 0")
    @Max(value = Long.MAX_VALUE, message = "tenantId vượt quá giới hạn cho phép")
    private Long tenantId;

    @Schema(description = "Tỉnh kinh doanh", example = "HN")
    @Size(max = 5, message = "businessProvince tối đa 5 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,5}$", message = "businessProvince chỉ gồm chữ và số")
    private String businessProvince;

    @Schema(description = "Quận kinh doanh", example = "Ba Đình")
    @Size(max = 5, message = "businessDistrict tối đa 5 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,5}$", message = "businessDistrict chỉ gồm chữ và số")
    private String businessDistrict;

    @Schema(description = "Phường kinh doanh", example = "Phường 1")
    @Size(max = 5, message = "businessPrecinct tối đa 5 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,5}$", message = "businessPrecinct chỉ gồm chữ và số")
    private String businessPrecinct;

    @Schema(description = "Khối đường kinh doanh", example = "Phường 1")
    @Size(max = 100, message = "businessStreetBlock tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "businessStreetBlock không được chứa ký tự điều khiển")
    private String businessStreetBlock;

    @Schema(description = "Đường kinh doanh", example = "Nguyễn Trãi")
    @Size(max = 100, message = "businessStreet tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "businessStreet không được chứa ký tự điều khiển")
    private String businessStreet;

    @Schema(description = "Số nhà kinh doanh", example = "456")
    @Size(max = 100, message = "businessHome tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "businessHome không được chứa ký tự điều khiển")
    private String businessHome;

    @Schema(description = "Mã vùng kinh doanh", example = "HN")
    @Size(max = 50, message = "businessAreacode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "businessAreacode chỉ gồm chữ và số")
    private String businessAreacode;

    @Schema(description = "Địa chỉ kinh doanh", example = "456 Nguyễn Trãi, Ba Đình, Hà Nội")
    @Size(max = 500, message = "businessAddress tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "businessAddress không được chứa ký tự điều khiển")
    private String businessAddress;

    @Schema(description = "Người tạo", example = "admin")
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Người cập nhật", example = "admin")
    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "Ngày tạo", example = "2024-01-01")
    private Date createDatetime;

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 1, message = "status tối đa 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Doanh thu", example = "Y")
    @Size(max = 1, message = "turnover tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "turnover chỉ gồm chữ hoặc số")
    private String turnover;

    @Schema(description = "Ngày sinh", example = "1990-01-01")
    private Date birthday;

    @Schema(description = "Loại kênh của cửa hàng có phải kênh đại lý hiệu lực bán hay không", example = "false")
    private Boolean isChannelOfAgent;

    // --- Getters ---

    public Long getShopId() {
        return shopId;
    }

    public String getName() {
        return name;
    }

    public Long getParentShopId() {
        return parentShopId;
    }

    public String getAccount() {
        return account;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAddress() {
        return address;
    }

    public String getTel() {
        return tel;
    }

    public String getFax() {
        return fax;
    }

    public String getShopCode() {
        return shopCode;
    }

    public String getShopType() {
        return shopType;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactTitle() {
        return contactTitle;
    }

    public String getTelNumber() {
        return telNumber;
    }

    public String getEmail() {
        return email;
    }

    public String getDescription() {
        return description;
    }

    public String getProvince() {
        return province;
    }

    public String getParShopCode() {
        return parShopCode;
    }

    public String getCenterCode() {
        return centerCode;
    }

    public String getOldShopCode() {
        return oldShopCode;
    }

    public String getCompany() {
        return company;
    }

    public String getTin() {
        return tin;
    }

    public String getShop() {
        return shop;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public String getPayComm() {
        return payComm;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Long getChannelTypeId() {
        return channelTypeId;
    }

    public String getDiscountPolicy() {
        return discountPolicy;
    }

    public String getPricePolicy() {
        return pricePolicy;
    }

    public String getShopPath() {
        return shopPath;
    }

    public String getDistrict() {
        return district;
    }

    public String getPrecinct() {
        return precinct;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public String getIdNo() {
        return idNo;
    }

    public String getIdIssuePlace() {
        return idIssuePlace;
    }

    public Date getIdIssueDate() {
        return idIssueDate;
    }

    public String getStreetBlock() {
        return streetBlock;
    }

    public String getStreet() {
        return street;
    }

    public String getHome() {
        return home;
    }

    public Integer getIdType() {
        return idType;
    }

    public String getShopPathName() {
        return shopPathName;
    }

    public String getContractNo() {
        return contractNo;
    }

    public String getFileName() {
        return fileName;
    }

    public String getBusinessLicence() {
        return businessLicence;
    }

    public String getBankplusMobile() {
        return bankplusMobile;
    }

    public Integer getStockNum() {
        return stockNum;
    }

    public Integer getStockNumImp() {
        return stockNumImp;
    }

    public Date getUpdateDateTime() {
        return updateDateTime;
    }

    public String getBankCode() {
        return bankCode;
    }

    public Long getShopKeeperId() {
        return shopKeeperId;
    }

    public Long getShopDirectorId() {
        return shopDirectorId;
    }

    public Long getGroupChannelTypeId() {
        return groupChannelTypeId;
    }

    public Long getStaffOwnerId() {
        return staffOwnerId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getBusinessProvince() {
        return businessProvince;
    }

    public String getBusinessDistrict() {
        return businessDistrict;
    }

    public String getBusinessPrecinct() {
        return businessPrecinct;
    }

    public String getBusinessStreetBlock() {
        return businessStreetBlock;
    }

    public String getBusinessStreet() {
        return businessStreet;
    }

    public String getBusinessHome() {
        return businessHome;
    }

    public String getBusinessAreacode() {
        return businessAreacode;
    }

    public String getBusinessAddress() {
        return businessAddress;
    }

    public String getCreateUser() {
        return createUser;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public Date getCreateDatetime() {
        return createDatetime;
    }

    public String getStatus() {
        return status;
    }

    public String getTurnover() {
        return turnover;
    }

    public Date getBirthday() {
        return birthday;
    }

    public Boolean getIsChannelOfAgent() {
        return isChannelOfAgent;
    }


    // --- Setters ---

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentShopId(Long parentShopId) {
        this.parentShopId = parentShopId;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public void setShopCode(String shopCode) {
        this.shopCode = shopCode;
    }

    public void setShopType(String shopType) {
        this.shopType = shopType;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setContactTitle(String contactTitle) {
        this.contactTitle = contactTitle;
    }

    public void setTelNumber(String telNumber) {
        this.telNumber = telNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setParShopCode(String parShopCode) {
        this.parShopCode = parShopCode;
    }

    public void setCenterCode(String centerCode) {
        this.centerCode = centerCode;
    }

    public void setOldShopCode(String oldShopCode) {
        this.oldShopCode = oldShopCode;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setTin(String tin) {
        this.tin = tin;
    }

    public void setShop(String shop) {
        this.shop = shop;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public void setPayComm(String payComm) {
        this.payComm = payComm;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public void setChannelTypeId(Long channelTypeId) {
        this.channelTypeId = channelTypeId;
    }

    public void setDiscountPolicy(String discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    public void setPricePolicy(String pricePolicy) {
        this.pricePolicy = pricePolicy;
    }

    public void setShopPath(String shopPath) {
        this.shopPath = shopPath;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setPrecinct(String precinct) {
        this.precinct = precinct;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public void setIdIssuePlace(String idIssuePlace) {
        this.idIssuePlace = idIssuePlace;
    }

    public void setIdIssueDate(Date idIssueDate) {
        this.idIssueDate = idIssueDate;
    }

    public void setStreetBlock(String streetBlock) {
        this.streetBlock = streetBlock;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public void setIdType(Integer idType) {
        this.idType = idType;
    }

    public void setShopPathName(String shopPathName) {
        this.shopPathName = shopPathName;
    }

    public void setContractNo(String contractNo) {
        this.contractNo = contractNo;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setBusinessLicence(String businessLicence) {
        this.businessLicence = businessLicence;
    }

    public void setBankplusMobile(String bankplusMobile) {
        this.bankplusMobile = bankplusMobile;
    }

    public void setStockNum(Integer stockNum) {
        this.stockNum = stockNum;
    }

    public void setStockNumImp(Integer stockNumImp) {
        this.stockNumImp = stockNumImp;
    }

    public void setUpdateDateTime(Date updateDateTime) {
        this.updateDateTime = updateDateTime;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public void setShopKeeperId(Long shopKeeperId) {
        this.shopKeeperId = shopKeeperId;
    }

    public void setShopDirectorId(Long shopDirectorId) {
        this.shopDirectorId = shopDirectorId;
    }

    public void setGroupChannelTypeId(Long groupChannelTypeId) {
        this.groupChannelTypeId = groupChannelTypeId;
    }

    public void setStaffOwnerId(Long staffOwnerId) {
        this.staffOwnerId = staffOwnerId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public void setBusinessProvince(String businessProvince) {
        this.businessProvince = businessProvince;
    }

    public void setBusinessDistrict(String businessDistrict) {
        this.businessDistrict = businessDistrict;
    }

    public void setBusinessPrecinct(String businessPrecinct) {
        this.businessPrecinct = businessPrecinct;
    }

    public void setBusinessStreetBlock(String businessStreetBlock) {
        this.businessStreetBlock = businessStreetBlock;
    }

    public void setBusinessStreet(String businessStreet) {
        this.businessStreet = businessStreet;
    }

    public void setBusinessHome(String businessHome) {
        this.businessHome = businessHome;
    }

    public void setBusinessAreacode(String businessAreacode) {
        this.businessAreacode = businessAreacode;
    }

    public void setBusinessAddress(String businessAddress) {
        this.businessAddress = businessAddress;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
    }

    public void setCreateDatetime(Date createDatetime) {
        this.createDatetime = createDatetime;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setTurnover(String turnover) {
        this.turnover = turnover;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public void setIsChannelOfAgent(Boolean isChannelOfAgent) {
        this.isChannelOfAgent = isChannelOfAgent;
    }

}
