package com.viettel.bccs.organization.staff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài/precision cột thật của STAFF (xem StaffEntity)
 * khi field ánh xạ 1-1 vào cột DB; field "view"/tổng hợp không có cột DB dùng biên suy luận hợp lý.
 * Field không nullable ở DB vẫn cho phép null ở DTO, @Size/@Pattern/@Min/@Max chỉ áp dụng khi giá trị khác null.
 *
 * Lưu ý: field `staffApp` (List&lt;StaffDTO&gt;) đã bị XOÁ khỏi DTO này — đây là field tự tham chiếu
 * (cycle vi phạm quy tắc DTO) nhưng không có bất kỳ usage thật nào (getStaffApp/setStaffApp) ngoài
 * chính class này trong toàn bộ module main, nên được dọn bỏ an toàn.
 */
@Schema
public class StaffDTO implements Serializable {

    public StaffDTO() {
    }

    public StaffDTO(String staffCode) {
        this.staffCode = staffCode;
    }

    public static enum FILTER {staffCode, name, staffId, status}

    public static enum COLUMNS {address, areaCode, bankplusMobile, birthday, btsCode, businessLicence,
        businessMethod, channelTypeId, contractFromDate, contractMethod, contractNo, depositValue,
        discountPolicy, district, email, fileName, hasEquipment, hasTin, home, idIssueDate,
        idIssuePlace, idNo, idType, imsi, isdn, lastLockTime, lastModified, lockFlag, lockStatus, name, note,
        paymentLimit, paymentUsage, pin, pointOfSale, pointOfSaleType, precinct, pricePolicy, province, registerInfo, serial,
        shopId, shopOwnerId, staffCode, staffId, staffOwnType, staffOwnerId, subOwnerId, subOwnerType, tel, tin, ttnsCode, type, userId, warningContent, mapAreaChainChannel, createUser, installationStatus}

    @Schema(description = "Ten trang thai", example = "Dang hoat dong")
    @Size(max = 100, message = "statusName toi da 100 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "statusName khong duoc chua ky tu dieu khien")
    private String statusName;
    @Schema(description = "So bankplus", example = "0909123456")
    @Size(max = 20, message = "bankplusMobile toi da 20 ky tu")
    @Pattern(regexp = "^[0-9+\\-\\s]{0,20}$", message = "bankplusMobile chi gom so, '+', '-' hoac khoang trang")
    private String bankplusMobile;
    @Schema(description = "Ngay sinh", example = "1990-01-01")
    private Date birthday;
    @Schema(description = "Ma BTS", example = "BTS_HN_001")
    @Size(max = 10, message = "btsCode toi da 10 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "btsCode chi gom chu, so, '_' hoac '-'")
    private String btsCode;
    @Schema(description = "Giay phep kinh doanh", example = "GPKD_123456")
    @Size(max = 200, message = "businessLicence toi da 200 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,200}$", message = "businessLicence chi gom chu, so, '_' hoac '-'")
    private String businessLicence;
    @Schema(description = "Phuong thuc kinh doanh", example = "1")
    @Min(value = 0, message = "businessMethod phai >= 0")
    @Max(value = 99, message = "businessMethod vuot qua do dai cot (precision 2)")
    private Long businessMethod;
    @Schema(description = "ID loai kenh", example = "1")
    @Min(value = 0, message = "channelTypeId phai >= 0")
    @Max(value = 9999999999L, message = "channelTypeId vuot qua do dai cot (precision 10)")
    private Long channelTypeId;
    @Schema(description = "Ngay bat dau hop dong", example = "2024-01-01")
    private Date contractFromDate;
    @Schema(description = "Phuong thuc hop dong", example = "1")
    @Min(value = 0, message = "contractMethod phai >= 0")
    @Max(value = 99, message = "contractMethod vuot qua do dai cot (precision 2)")
    private Long contractMethod;
    @Schema(description = "So hop dong", example = "HD_2024_001")
    @Size(max = 100, message = "contractNo toi da 100 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "contractNo chi gom chu, so, '_' hoac '-'")
    private String contractNo;
    @Schema(description = "So tai khoan nhan dien", example = "1234567890")
    @Size(max = 50, message = "identifyAccountNo toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "identifyAccountNo chi gom chu, so, '_' hoac '-'")
    private String identifyAccountNo;
    @Schema(description = "Ten tai khoan nhan dien", example = "Nguyen Van A")
    @Size(max = 300, message = "identifyAccountName toi da 300 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "identifyAccountName khong duoc chua ky tu dieu khien")
    private String identifyAccountName;
    @Schema(description = "Loai phe duyet", example = "APPROVE")
    @Size(max = 50, message = "approvalType toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "approvalType chi gom chu, so, '_' hoac '-'")
    private String approvalType;
    @Schema(description = "Ngay ket thuc hop dong", example = "2025-01-01")
    private Date contractToDate;
    @Schema(description = "Gia tri dat coc", example = "5000000")
    @Min(value = 0, message = "depositValue phai >= 0")
    @Max(value = 9999999999L, message = "depositValue vuot qua do dai cot (precision 10)")
    private Long depositValue;
    @Schema(description = "Chinh sach giam gia", example = "DISC_001")
    @Size(max = 50, message = "discountPolicy toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "discountPolicy chi gom chu, so, '_' hoac '-'")
    private String discountPolicy;
    @Schema(description = "Quan/huyen", example = "Ba Dinh")
    @Size(max = 50, message = "district toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "district chi gom chu va so")
    private String district;
    @Schema(description = "Email", example = "nguyenvana@viettel.vn")
    @Size(max = 100, message = "email toi da 100 ky tu")
    @Pattern(regexp = "^$|^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "email khong dung dinh dang")
    private String email;
    @Schema(description = "Dia chi", example = "123 Nguyen Trai, Quan 1, TP HCM")
    @Size(max = 500, message = "address toi da 500 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "address khong duoc chua ky tu dieu khien")
    private String address;
    @Schema(description = "Ten file dinh kem", example = "hop_dong.pdf")
    @Size(max = 200, message = "fileName toi da 200 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "fileName khong duoc chua ky tu dieu khien")
    private String fileName;
    @Schema(description = "Gioi tinh", example = "1")
    @Min(value = 0, message = "gender phai >= 0")
    @Max(value = 99, message = "gender vuot qua do dai cot (precision 2)")
    private Long gender;
    @Schema(description = "Co thiet bi", example = "1")
    @Min(value = 0, message = "hasEquipment phai >= 0")
    @Max(value = 99, message = "hasEquipment vuot qua do dai cot (precision 2)")
    private Long hasEquipment;
    @Schema(description = "Co ma so thue", example = "1")
    @Min(value = 0, message = "hasTin phai >= 0")
    @Max(value = 99, message = "hasTin vuot qua do dai cot (precision 2)")
    private Long hasTin;
    @Schema(description = "Ngay cap CMND/CCCD", example = "2020-01-01")
    private Date idIssueDate;
    @Schema(description = "Noi cap CMND/CCCD", example = "Ha Noi")
    @Size(max = 150, message = "idIssuePlace toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "idIssuePlace khong duoc chua ky tu dieu khien")
    private String idIssuePlace;
    @Schema(description = "So CMND/CCCD", example = "001234567890")
    @Size(max = 20, message = "idNo toi da 20 ky tu")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "idNo chi gom chu so")
    private String idNo;
    @Schema(description = "La nhan vien moi", example = "Y")
    @Size(max = 1, message = "isNew toi da 1 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "isNew chi gom chu hoac so")
    private String isNew;
    @Schema(description = "Loai CMND/CCCD", example = "1")
    @Min(value = 0, message = "idType phai >= 0")
    @Max(value = 99, message = "idType vuot qua do dai cot (precision 2)")
    private Long idType;
    @Schema(description = "IMSI", example = "452011234567890")
    @Size(max = 20, message = "imsi toi da 20 ky tu")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "imsi chi gom chu so")
    private String imsi;
    @Schema(description = "So dien thoai ISDN", example = "0909123456")
    @Size(max = 15, message = "isdn toi da 15 ky tu")
    @Pattern(regexp = "^[0-9+\\-\\s]{0,15}$", message = "isdn chi gom so, '+', '-' hoac khoang trang")
    private String isdn;
    @Schema(description = "Thoi gian khoa cuoi", example = "2024-06-01")
    private Date lastLockTime;
    @Schema(description = "Lan cap nhat cuoi", example = "2024-06-01")
    private Date lastModified;
    @Schema(description = "Flag khoa", example = "1")
    @Size(max = 10, message = "lockFlag toi da 10 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9]{0,10}$", message = "lockFlag chi gom chu hoac so")
    private String lockFlag;
    @Schema(description = "Trang thai khoa", example = "0")
    @Min(value = 0, message = "lockStatus phai >= 0")
    @Max(value = 9999999999L, message = "lockStatus vuot qua do dai cot (precision 10)")
    private Long lockStatus;
    @Schema(name = "name", description = "Ten nhan vien/diem ban", example = "Nguyen Van A")
    @Size(max = 300, message = "name toi da 300 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "name khong duoc chua ky tu dieu khien")
    private String name;
    @Schema(description = "Ghi chu", example = "Ghi chu nhan vien")
    @Size(max = 500, message = "note toi da 500 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "note khong duoc chua ky tu dieu khien")
    private String note;
    @Schema(description = "Gioi han thanh toan", example = "50000000")
    @Min(value = 0, message = "paymentLimit phai >= 0")
    @Max(value = 9999999999L, message = "paymentLimit vuot qua do dai cot (precision 10)")
    private Long paymentLimit;
    @Schema(description = "Su dung thanh toan", example = "10000000")
    @Min(value = 0, message = "paymentUsage phai >= 0")
    @Max(value = 9999999999L, message = "paymentUsage vuot qua do dai cot (precision 10)")
    private Long paymentUsage;
    @Schema(description = "Ma PIN", example = "1234")
    @Size(max = 10, message = "pin toi da 10 ky tu")
    @Pattern(regexp = "^[0-9]{0,10}$", message = "pin chi gom chu so")
    private String pin;
    @Schema(description = "Diem ban hang", example = "POS_001")
    @Size(max = 5, message = "pointOfSale toi da 5 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,5}$", message = "pointOfSale chi gom chu, so, '_' hoac '-'")
    private String pointOfSale;
    @Schema(description = "Loai diem ban", example = "1")
    @Min(value = 0, message = "pointOfSaleType phai >= 0")
    @Max(value = 9, message = "pointOfSaleType vuot qua do dai cot (precision 1)")
    private Long pointOfSaleType;
    @Schema(description = "Chinh sach gia", example = "PRICE_001")
    @Size(max = 20, message = "pricePolicy toi da 20 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "pricePolicy chi gom chu, so, '_' hoac '-'")
    private String pricePolicy;
    @Schema(description = "Thong tin dang ky", example = "Dang ky tai khoan")
    @Size(max = 2, message = "registerInfo toi da 2 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "registerInfo chi gom chu hoac so")
    private String registerInfo;
    @Schema(description = "Serial", example = "SN123456789")
    @Size(max = 20, message = "serial toi da 20 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "serial chi gom chu, so, '_' hoac '-'")
    private String serial;
    @Schema(description = "ID cua hang/dai ly", example = "12345")
    @Min(value = 0, message = "shopId phai >= 0")
    @Max(value = 9999999999L, message = "shopId vuot qua do dai cot (precision 10)")
    private Long shopId;
    @Schema(description = "ID chu cua hang", example = "12345")
    @Min(value = 0, message = "shopOwnerId phai >= 0")
    @Max(value = Long.MAX_VALUE, message = "shopOwnerId vuot qua do dai cot (precision 20)")
    private Long shopOwnerId;
    @Schema(description = "Ma nhan vien/diem ban", example = "NV_001")
    @Size(max = 40, message = "staffCode toi da 40 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "staffCode chi gom chu, so, '_' hoac '-'")
    private String staffCode;
    @Schema(description = "ID nhan vien/diem ban", example = "12345")
    @Min(value = 0, message = "staffId phai >= 0")
    @Max(value = 9999999999L, message = "staffId vuot qua do dai cot (precision 10)")
    private Long staffId;
    @Schema(description = "Loai chu so huu", example = "OWNER")
    @Size(max = 1, message = "staffOwnType toi da 1 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "staffOwnType chi gom chu hoac so")
    private String staffOwnType;
    @Schema(description = "ID nhan vien quan ly", example = "12345")
    @Min(value = 0, message = "staffOwnerId phai >= 0")
    @Max(value = 9999999999L, message = "staffOwnerId vuot qua do dai cot (precision 10)")
    private Long staffOwnerId;
    @Schema(description = "Ma nhan vien quan ly", example = "NV_002")
    @Size(max = 40, message = "staffOwnerCode toi da 40 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "staffOwnerCode chi gom chu, so, '_' hoac '-'")
    private String staffOwnerCode;
    @Schema(description = "Ten nhan vien quan ly", example = "Tran Van B")
    @Size(max = 300, message = "staffOwnerName toi da 300 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "staffOwnerName khong duoc chua ky tu dieu khien")
    private String staffOwnerName;
    @Schema(description = "Dien thoai nhan vien quan ly", example = "0909123457")
    @Size(max = 15, message = "staffOwnerTel toi da 15 ky tu")
    @Pattern(regexp = "^[0-9+\\-\\s]{0,15}$", message = "staffOwnerTel chi gom so, '+', '-' hoac khoang trang")
    private String staffOwnerTel;
    @Schema(description = "Trang thai", example = "1")
    @Size(max = 1, message = "status toi da 1 ky tu")
    @Pattern(regexp = "^[01]$", message = "status chi nhan gia tri 0 hoac 1")
    private String status;
    @Schema(description = "So kho nhap", example = "100")
    @Min(value = 0, message = "stockNum phai >= 0")
    @Max(value = 9999999, message = "stockNum vuot qua do dai cot (precision 7)")
    private Long stockNum;
    @Schema(description = "So kho xuat", example = "50")
    @Min(value = 0, message = "stockNumImp phai >= 0")
    @Max(value = 9999999, message = "stockNumImp vuot qua do dai cot (precision 7)")
    private Long stockNumImp;
    @Schema(description = "Duong", example = "Nguyen Trai")
    @Size(max = 100, message = "street toi da 100 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "street khong duoc chua ky tu dieu khien")
    private String street;
    @Schema(description = "ID chu so huu phu", example = "12345")
    @Min(value = 0, message = "subOwnerId phai >= 0")
    @Max(value = Long.MAX_VALUE, message = "subOwnerId vuot qua do dai cot (precision 20)")
    private Long subOwnerId;
    @Schema(description = "Loai chu so huu phu", example = "SUB_OWNER")
    @Min(value = 0, message = "subOwnerType phai >= 0")
    @Max(value = 9, message = "subOwnerType vuot qua do dai cot (precision 1)")
    private Long subOwnerType;
    @Schema(description = "So dien thoai lien he", example = "0909123456")
    @Size(max = 15, message = "tel toi da 15 ky tu")
    @Pattern(regexp = "^[0-9+\\-\\s]{0,15}$", message = "tel chi gom so, '+', '-' hoac khoang trang")
    private String tel;
    @Schema(description = "Ma so thue", example = "0123456789")
    @Size(max = 50, message = "tin toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "tin chi gom chu, so, '_' hoac '-'")
    private String tin;
    @Schema(description = "Ma thong tin nhan su", example = "TTNS_001")
    @Size(max = 200, message = "ttnsCode toi da 200 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,200}$", message = "ttnsCode chi gom chu, so, '_' hoac '-'")
    private String ttnsCode;
    @Schema(description = "Loai nhan vien", example = "1")
    @Min(value = 0, message = "type phai >= 0")
    @Max(value = 9999999999L, message = "type vuot qua do dai cot (precision 10)")
    private Long type;
    @Schema(description = "ID nguoi dung", example = "12345")
    @Min(value = 0, message = "userId phai >= 0")
    @Max(value = Long.MAX_VALUE, message = "userId vuot qua do dai cot (precision 20)")
    private Long userId;
    @Schema(description = "Noi dung canh bao", example = "Canh bao tai khoan")
    @Size(max = 500, message = "warningContent toi da 500 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "warningContent khong duoc chua ky tu dieu khien")
    private String warningContent;
    @Schema(description = "Ma cua hang", example = "VTST_HN_001")
    @Size(max = 40, message = "shopCode toi da 40 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "shopCode chi gom chu, so, '_' hoac '-'")
    private String shopCode;
    @Schema(description = "Ten cua hang", example = "Viettel Store Ha Noi")
    @Size(max = 300, message = "shopName toi da 300 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "shopName khong duoc chua ky tu dieu khien")
    private String shopName;
    @Schema(description = "La diem ban hang", example = "true")
    private boolean isPointOfSale;
    @Schema(description = "ID loai kenh cua hang", example = "1")
    @Min(value = 0, message = "shopChanelTypeId phai >= 0")
    @Max(value = 9999999999L, message = "shopChanelTypeId vuot qua do dai cot (precision 10)")
    private Long shopChanelTypeId;
    @Schema(description = "Tinh/thanh pho cua hang", example = "Ha Noi")
    @Size(max = 50, message = "shopProvince toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "shopProvince chi gom chu va so")
    private String shopProvince;
    @Schema(description = "Quan/huyen cua hang", example = "Ba Dinh")
    @Size(max = 50, message = "shopDistrict toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "shopDistrict chi gom chu va so")
    private String shopDistrict;
    @Schema(description = "Phuong/xa cua hang", example = "Phuong 1")
    @Size(max = 50, message = "shopPrecinct toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "shopPrecinct chi gom chu va so")
    private String shopPrecinct;
    @Schema(description = "Duong dan cua hang", example = "/HN/VTST_HN_001")
    @Size(max = 500, message = "shopPath toi da 500 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "shopPath khong duoc chua ky tu dieu khien")
    private String shopPath;
    @Schema(description = "ID nhan vien giao dich", example = "12345")
    @Min(value = 0, message = "saleTransStaffId phai >= 0")
    @Max(value = 9999999999L, message = "saleTransStaffId vuot qua do dai cot (precision 10)")
    private Long saleTransStaffId;
    @Schema(description = "Dia chi IP", example = "192.168.1.1")
    @Size(max = 45, message = "ipAddress toi da 45 ky tu")
    @Pattern(regexp = "^[0-9a-fA-F.:]{0,45}$", message = "ipAddress khong dung dinh dang")
    private String ipAddress;
    @Schema(description = "Khoa chinh bang", example = "staff_id")
    @Size(max = 50, message = "tablePk toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "tablePk chi gom chu, so, '_' hoac '-'")
    private String tablePk;
    @Schema(description = "Ten loai kenh", example = "Dai ly")
    @Size(max = 150, message = "channelTypeName toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "channelTypeName khong duoc chua ky tu dieu khien")
    private String channelTypeName;
    @Schema(description = "Ten nhan vien quan ly", example = "Tran Van B")
    @Size(max = 300, message = "managerName toi da 300 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "managerName khong duoc chua ky tu dieu khien")
    private String managerName;
    @Schema(description = "Loai cua hang", example = "1")
    @Size(max = 1, message = "shopType toi da 1 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "shopType chi gom chu hoac so")
    private String shopType;
    @Schema(description = "Hanh dong", example = "CREATE")
    @Size(max = 20, message = "action toi da 20 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "action chi gom chu, so, '_' hoac '-'")
    private String action;
    @Schema(description = "So SDN", example = "SDN_001")
    @Size(max = 20, message = "sdnNumber toi da 20 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "sdnNumber chi gom chu, so, '_' hoac '-'")
    private String sdnNumber;
    @Schema(description = "Cua hang la dai ly", example = "true")
    private Boolean shopIsAgent;
    @Schema(description = "ID nhom loai kenh", example = "5")
    @Min(value = 0, message = "groupChannelTypeId phai >= 0")
    @Max(value = 9999999999L, message = "groupChannelTypeId vuot qua do dai cot (precision 10)")
    private Long groupChannelTypeId;
    @Schema(description = "Ma chuc danh", example = "POS_001")
    @Size(max = 50, message = "positionCode toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "positionCode chi gom chu, so, '_' hoac '-'")
    private String positionCode;
    @Schema(description = "Ten chuc danh", example = "Nhan vien ban hang")
    @Size(max = 150, message = "positionName toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "positionName khong duoc chua ky tu dieu khien")
    private String positionName;
    @Schema(description = "Ten to chuc", example = "Viettel Ha Noi")
    @Size(max = 300, message = "organizationName toi da 300 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "organizationName khong duoc chua ky tu dieu khien")
    private String organizationName;
    @Schema(description = "Ma ngan hang bankplus", example = "VCB")
    @Size(max = 50, message = "bankplusBankCode toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "bankplusBankCode chi gom chu, so, '_' hoac '-'")
    private String bankplusBankCode;
    @Schema(description = "Ma ngan hang cha", example = "VCB")
    @Size(max = 50, message = "parentBankCode toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "parentBankCode chi gom chu, so, '_' hoac '-'")
    private String parentBankCode;
    @Schema(description = "Ma CITAD", example = "CITAD_001")
    @Size(max = 50, message = "citadCode toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "citadCode chi gom chu, so, '_' hoac '-'")
    private String citadCode;
    @Schema(description = "So tai khoan", example = "1234567890")
    @Size(max = 50, message = "accountNumber toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "accountNumber chi gom chu, so, '_' hoac '-'")
    private String accountNumber;
    @Schema(description = "Chu tai khoan", example = "Nguyen Van A")
    @Size(max = 300, message = "accountOwner toi da 300 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "accountOwner khong duoc chua ky tu dieu khien")
    private String accountOwner;
    @Schema(description = "ID map shop bank CITAD", example = "12345")
    @Min(value = 0, message = "mapShopBankCitadId phai >= 0")
    @Max(value = Long.MAX_VALUE, message = "mapShopBankCitadId vuot qua gioi han cho phep")
    private Long mapShopBankCitadId;
    @Schema(description = "ID tenant", example = "1")
    @Min(value = 0, message = "tenantId phai >= 0")
    @Max(value = Long.MAX_VALUE, message = "tenantId vuot qua gioi han cho phep")
    private Long tenantId;
    @Schema(description = "Ten chinh sach gia", example = "Chinh sach gia 2024")
    @Size(max = 150, message = "pricePolicyName toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "pricePolicyName khong duoc chua ky tu dieu khien")
    private String pricePolicyName;
    @Schema(description = "Ten chinh sach giam gia", example = "Chinh sach giam gia 2024")
    @Size(max = 150, message = "discountPolicyName toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "discountPolicyName khong duoc chua ky tu dieu khien")
    private String discountPolicyName;
    @Schema(description = "Ten tinh/thanh pho", example = "Ha Noi")
    @Size(max = 150, message = "provinceName toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "provinceName khong duoc chua ky tu dieu khien")
    private String provinceName;
    @Schema(description = "Map vung chan", example = "HN_BD")
    @Size(max = 50, message = "mapAreaChainChannel toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "mapAreaChainChannel chi gom chu, so, '_' hoac '-'")
    private String mapAreaChainChannel;
    @Schema(description = "ID loai kenh phu", example = "2")
    @Min(value = 0, message = "subChannelTypeId phai >= 0")
    @Max(value = 9999999999L, message = "subChannelTypeId vuot qua do dai cot (precision 10)")
    private Long subChannelTypeId;
    @Schema(description = "Yeu cau VSA", example = "false")
    private boolean vsaRequest = false;
    @Schema(description = "Da dang ky viettel wallet", example = "true")
    private boolean isWalletRegister;
    @Schema(description = "Ten quan/huyen", example = "Ba Dinh")
    @Size(max = 150, message = "districtName toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "districtName khong duoc chua ky tu dieu khien")
    private String districtName;
    @Schema(description = "Ten phuong/xa", example = "Phuong 1")
    @Size(max = 150, message = "precinctName toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "precinctName khong duoc chua ky tu dieu khien")
    private String precinctName;
    @Schema(description = "Du lieu nhi phan", example = "...")
    @Size(max = 5_000_000, message = "data toi da 5MB")
    private byte[] data;
    @Schema(description = "Kiem tra map nhom vi tri", example = "false")
    private boolean checkMapGroupPos;
    @Schema(description = "Danh sach ID kenh", example = "[1, 2, 3]")
    @Size(max = 1000, message = "channelList toi da 1000 phan tu")
    private List<Long> channelList;
    @Schema(description = "Ma nhom vi tri", example = "PG_001")
    @Size(max = 50, message = "positionGroup toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "positionGroup chi gom chu, so, '_' hoac '-'")
    private String positionGroup;
    @Schema(description = "Ten nhom vi tri", example = "Nhom vi tri ban hang")
    @Size(max = 150, message = "positionGroupName toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "positionGroupName khong duoc chua ky tu dieu khien")
    private String positionGroupName;
    @Schema(description = "Kiem tra", example = "Y")
    @Size(max = 10, message = "check toi da 10 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9]{0,10}$", message = "check chi gom chu hoac so")
    private String check;
    @Schema(description = "Nguoi tao", example = "admin")
    @Size(max = 50, message = "createUser toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chi gom chu, so, '.', '_' hoac '-'")
    private String createUser;
    @Schema(description = "Nguoi cap nhat", example = "admin")
    @Size(max = 50, message = "updateUser toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chi gom chu, so, '.', '_' hoac '-'")
    private String updateUser;
    @Schema(description = "Ngay tao", example = "2024-01-01")
    private Date createDatetime;
    @Schema(description = "Ngay cap nhat", example = "2024-06-01")
    private Date updateDatetime;
    @Schema(description = "ID to chuc", example = "12345")
    @Size(max = 50, message = "organizationId toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "organizationId chi gom chu, so, '_' hoac '-'")
    private String organizationId;
    @Schema(description = "ID cua hang cha", example = "12345")
    @Min(value = 0, message = "parentShopId phai >= 0")
    @Max(value = 9999999999L, message = "parentShopId vuot qua do dai cot (precision 10)")
    private Long parentShopId;
    @Schema(description = "Ten thuong hieu", example = "Viettel")
    @Size(max = 150, message = "brandName toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "brandName khong duoc chua ky tu dieu khien")
    private String brandName;
    @Schema(description = "Cap bac", example = "1")
    @Min(value = 0, message = "lv phai >= 0")
    @Max(value = 999, message = "lv vuot qua gioi han cho phep")
    private Long lv;
    @Schema(description = "Ma ngan hang cha", example = "VCB")
    @Size(max = 50, message = "parenBank toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "parenBank chi gom chu, so, '_' hoac '-'")
    private String parenBank;
    @Schema(description = "Dia chi theo CMND", example = "123 Nguyen Trai, Ba Dinh, Ha Noi")
    @Size(max = 500, message = "addressIdNo toi da 500 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "addressIdNo khong duoc chua ky tu dieu khien")
    private String addressIdNo;
    @Schema(description = "Loai hinh kinh doanh", example = "1")
    @Size(max = 20, message = "businessType toi da 20 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "businessType chi gom chu, so, '_' hoac '-'")
    private String businessType;
    @Schema(description = "Tong so ban ghi", example = "100")
    @Size(max = 20, message = "totalRecords toi da 20 ky tu")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "totalRecords chi gom chu so")
    private String totalRecords;
    @Schema(description = "Ma so thue cong ty", example = "0123456789")
    @Size(max = 50, message = "companyTax toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "companyTax chi gom chu, so, '_' hoac '-'")
    private String companyTax;
    @Schema(description = "Loai thue", example = "1")
    @Min(value = 0, message = "taxType phai >= 0")
    @Max(value = 99, message = "taxType vuot qua gioi han cho phep")
    private Long taxType;
    @Schema(description = "So giay phep kinh doanh", example = "GPKD_001")
    @Size(max = 100, message = "companyBusinessNo toi da 100 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "companyBusinessNo chi gom chu, so, '_' hoac '-'")
    private String companyBusinessNo;
    @Schema(description = "Chuc danh dai dien", example = "Giam doc")
    @Size(max = 150, message = "companyRepresentPosition toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "companyRepresentPosition khong duoc chua ky tu dieu khien")
    private String companyRepresentPosition;
    @Schema(description = "So thu tu", example = "1")
    @Min(value = 0, message = "rowNum phai >= 0")
    @Max(value = Long.MAX_VALUE, message = "rowNum vuot qua gioi han cho phep")
    private Long rowNum;
    @Schema(description = "Ma kenh", example = "CH_001")
    @Size(max = 50, message = "channelCode toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "channelCode chi gom chu, so, '_' hoac '-'")
    private String channelCode;
    @Schema(description = "Ma kho", example = "STOCK_001")
    @Size(max = 40, message = "stockCode toi da 40 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "stockCode chi gom chu, so, '_' hoac '-'")
    private String stockCode;
    @Schema(description = "CMND/CCCD", example = "001234567890")
    @Size(max = 20, message = "identityCard toi da 20 ky tu")
    @Pattern(regexp = "^[0-9]{0,20}$", message = "identityCard chi gom chu so")
    private String identityCard;
    @Schema(description = "Ngay cap", example = "2020-01-01")
    private Date issueDate;
    @Schema(description = "Noi cap", example = "Ha Noi")
    @Size(max = 150, message = "issuePlace toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "issuePlace khong duoc chua ky tu dieu khien")
    private String issuePlace;
    @Schema(description = "So dien thoai", example = "0909123456")
    @Size(max = 20, message = "phoneNumber toi da 20 ky tu")
    @Pattern(regexp = "^[0-9+\\-\\s]{0,20}$", message = "phoneNumber chi gom so, '+', '-' hoac khoang trang")
    private String phoneNumber;
    @Schema(description = "Danh sach ID vai tro", example = "[\"ROLE_001\", \"ROLE_002\"]")
    @Size(max = 200, message = "roleIds toi da 200 phan tu")
    private List<String> roleIds;
    @Schema(description = "Ho va ten day du", example = "Nguyen Van A")
    @Size(max = 300, message = "fullName toi da 300 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "fullName khong duoc chua ky tu dieu khien")
    private String fullName;
    @Schema(description = "ID loai kenh cua nhan vien", example = "1")
    @Min(value = 0, message = "channelTypeOfStaff phai >= 0")
    @Max(value = 9999999999L, message = "channelTypeOfStaff vuot qua do dai cot (precision 10)")
    private Long channelTypeOfStaff;
    @Schema(description = "Ten loai kenh cua nhan vien", example = "Dai ly")
    @Size(max = 150, message = "nameOfChannelStaff toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "nameOfChannelStaff khong duoc chua ky tu dieu khien")
    private String nameOfChannelStaff;
    @Schema(description = "ID loai kenh cua cua hang", example = "1")
    @Min(value = 0, message = "channelTypeOfShop phai >= 0")
    @Max(value = 9999999999L, message = "channelTypeOfShop vuot qua do dai cot (precision 10)")
    private Long channelTypeOfShop;
    @Schema(description = "Ten loai kenh cua cua hang", example = "Dai ly")
    @Size(max = 150, message = "nameOfChannelShop toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "nameOfChannelShop khong duoc chua ky tu dieu khien")
    private String nameOfChannelShop;
    @Schema(description = "Gioi thieu cong ty", example = "Cong ty Viettel")
    @Size(max = 2000, message = "companyPresent toi da 2000 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,2000}$", message = "companyPresent khong duoc chua ky tu dieu khien")
    private String companyPresent;
    @Schema(description = "Dia chi hoat dong", example = "123 Nguyen Trai, Ba Dinh, Ha Noi")
    @Size(max = 500, message = "activeAddress toi da 500 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "activeAddress khong duoc chua ky tu dieu khien")
    private String activeAddress;
    @Schema(description = "Ngay sinh", example = "1990-01-01")
    @Size(max = 20, message = "birthDay toi da 20 ky tu")
    @Pattern(regexp = "^[0-9/\\-]{0,20}$", message = "birthDay chi gom so, '/' hoac '-'")
    private String birthDay;
    @Schema(description = "Ngay cap AI", example = "2020-01-01")
    @Size(max = 20, message = "idIssueDateAI toi da 20 ky tu")
    @Pattern(regexp = "^[0-9/\\-]{0,20}$", message = "idIssueDateAI chi gom so, '/' hoac '-'")
    private String idIssueDateAI;
    @Schema(description = "File CMND/CCCD mat truoc", example = "cmnd_mat_truoc.jpg")
    @Size(max = 200, message = "fileIdentifyId toi da 200 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "fileIdentifyId khong duoc chua ky tu dieu khien")
    private String fileIdentifyId;
    @Schema(description = "File chan dung", example = "chan_dung.jpg")
    @Size(max = 200, message = "filePortraitId toi da 200 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "filePortraitId khong duoc chua ky tu dieu khien")
    private String filePortraitId;
    @Schema(description = "File CMND/CCCD mat sau", example = "cmnd_mat_sau.jpg")
    @Size(max = 200, message = "fileIdentifyIdUp toi da 200 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "fileIdentifyIdUp khong duoc chua ky tu dieu khien")
    private String fileIdentifyIdUp;
    @Schema(description = "File CMND/CCCD un", example = "cmnd_un.jpg")
    @Size(max = 200, message = "fileIdentifyIdUn toi da 200 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "fileIdentifyIdUn khong duoc chua ky tu dieu khien")
    private String fileIdentifyIdUn;
    @Schema(description = "Mo ta", example = "Mo ta nhan vien")
    @Size(max = 1000, message = "description toi da 1000 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "description khong duoc chua ky tu dieu khien")
    private String description;
    @Schema(description = "Ngay hieu luc", example = "2024-01-01")
    private Date effectDatetime;
    @Schema(description = "Ngay het han", example = "2025-01-01")
    private Date expireDatetime;
    @Schema(description = "Ten dang nhap", example = "nguyenvana")
    @Size(max = 50, message = "username toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "username chi gom chu, so, '.', '_' hoac '-'")
    private String username;
    @Schema(description = "Trang thai cai dat", example = "INSTALLED")
    @Size(max = 50, message = "installationStatus toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "installationStatus chi gom chu, so, '_' hoac '-'")
    private String installationStatus;
    @Schema(description = "Can kiem tra tat ca vai tro", example = "false")
    private boolean needCheckAllRole;
    @Schema(description = "La chu so huu", example = "true")
    private boolean owner;
    @Schema(description = "Tin nhan tu SAP", example = "SAP_001")
    @Size(max = 500, message = "sapMessage toi da 500 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "sapMessage khong duoc chua ky tu dieu khien")
    private String sapMessage;
    @Schema(description = "Gui tin nhan", example = "false")
    private boolean sendMessage;
    @Schema(description = "Danh sach ma vung", example = "[\"HN\", \"HCM\"]")
    @Size(max = 200, message = "lstAreaCode toi da 200 phan tu")
    private List<String> lstAreaCode;
    @Schema(description = "Ten tinh/thanh pho cua hang", example = "Ha Noi")
    @Size(max = 150, message = "shopProvinceName toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "shopProvinceName khong duoc chua ky tu dieu khien")
    private String shopProvinceName;
    @Schema(description = "Loai thong tin nhan vien", example = "FULL_TIME")
    @Size(max = 50, message = "staffInfoType toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "staffInfoType chi gom chu, so, '_' hoac '-'")
    private String staffInfoType;
    @Schema(description = "My Viettel Order", example = "true")
    private Boolean myViettelOrder;
    @Schema(description = "Ghi chu cap nhat", example = "Cap nhat thong tin nhan vien")
    @Size(max = 500, message = "updateNote toi da 500 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "updateNote khong duoc chua ky tu dieu khien")
    private String updateNote;
    @Schema(description = "Khoa", example = "false")
    private boolean disable = false;
    @Schema(description = "Ngay kinh doanh cong ty", example = "2020-01-01")
    private Date companyBusinessDate;
    @Schema(description = "Noi kinh doanh cong ty", example = "Ha Noi")
    @Size(max = 300, message = "companyBusinessPlace toi da 300 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "companyBusinessPlace khong duoc chua ky tu dieu khien")
    private String companyBusinessPlace;
    @Schema(description = "Ten chuong trinh", example = "CT_001")
    @Size(max = 150, message = "programName toi da 150 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "programName khong duoc chua ky tu dieu khien")
    private String programName;
    @Schema(description = "URL hien tai", example = "/staff/detail")
    @Size(max = 500, message = "currentURL toi da 500 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "currentURL khong duoc chua ky tu dieu khien")
    private String currentURL;
    @Schema(description = "Tinh/thanh pho", example = "Ha Noi")
    @Size(max = 50, message = "province toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "province chi gom chu va so")
    private String province;
    @Schema(description = "Ma vung", example = "HN")
    @Size(max = 200, message = "areaCode toi da 200 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9]{0,200}$", message = "areaCode chi gom chu va so")
    private String areaCode;
    @Schema(description = "Khoi duong", example = "Phuong 1")
    @Size(max = 100, message = "streetBlock toi da 100 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "streetBlock khong duoc chua ky tu dieu khien")
    private String streetBlock;
    @Schema(description = "So nha", example = "123")
    @Size(max = 100, message = "home toi da 100 ky tu")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "home khong duoc chua ky tu dieu khien")
    private String home;
    @Schema(description = "Phuong/xa", example = "Phuong 1")
    @Size(max = 50, message = "precinct toi da 50 ky tu")
    @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "precinct chi gom chu va so")
    private String precinct;
    @Schema(description = "Thong tin cua hang", example = "...")
    private com.viettel.bccs.organization.shop.dto.ShopDTO shop;
    @Schema(description = "Loại kênh của nhân viên có phải kênh điểm bán hiệu lực bán hay không", example = "false")
    private Boolean isChannelOfSalePoint;

    // --- Getters ---

    public String getStatusName() { return statusName; }
    public String getBankplusMobile() { return bankplusMobile; }
    public Date getBirthday() { return birthday; }
    public String getBtsCode() { return btsCode; }
    public String getBusinessLicence() { return businessLicence; }
    public Long getBusinessMethod() { return businessMethod; }
    public Long getChannelTypeId() { return channelTypeId; }
    public Date getContractFromDate() { return contractFromDate; }
    public Long getContractMethod() { return contractMethod; }
    public String getContractNo() { return contractNo; }
    public String getIdentifyAccountNo() { return identifyAccountNo; }
    public String getIdentifyAccountName() { return identifyAccountName; }
    public String getApprovalType() { return approvalType; }
    public Date getContractToDate() { return contractToDate; }
    public Long getDepositValue() { return depositValue; }
    public String getDiscountPolicy() { return discountPolicy; }
    public String getDistrict() { return district; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getFileName() { return fileName; }
    public Long getGender() { return gender; }
    public Long getHasEquipment() { return hasEquipment; }
    public Long getHasTin() { return hasTin; }
    public Date getIdIssueDate() { return idIssueDate; }
    public String getIdIssuePlace() { return idIssuePlace; }
    public String getIdNo() { return idNo; }
    public String getIsNew() { return isNew; }
    public Long getIdType() { return idType; }
    public String getImsi() { return imsi; }
    public String getIsdn() { return isdn; }
    public Date getLastLockTime() { return lastLockTime; }
    public Date getLastModified() { return lastModified; }
    public String getLockFlag() { return lockFlag; }
    public Long getLockStatus() { return lockStatus; }
    public String getName() { return name; }
    public String getNote() { return note; }
    public Long getPaymentLimit() { return paymentLimit; }
    public Long getPaymentUsage() { return paymentUsage; }
    public String getPin() { return pin; }
    public String getPointOfSale() { return pointOfSale; }
    public Long getPointOfSaleType() { return pointOfSaleType; }
    public String getPricePolicy() { return pricePolicy; }
    public String getProvince() { return province; }
    public String getAreaCode() { return areaCode; }
    public String getStreetBlock() { return streetBlock; }
    public String getHome() { return home; }
    public String getPrecinct() { return precinct; }
    public String getRegisterInfo() { return registerInfo; }
    public String getSerial() { return serial; }
    public Long getShopId() { return shopId; }
    public Long getShopOwnerId() { return shopOwnerId; }
    public String getStaffCode() { return staffCode; }
    public Long getStaffId() { return staffId; }
    public String getStaffOwnType() { return staffOwnType; }
    public Long getStaffOwnerId() { return staffOwnerId; }
    public String getStaffOwnerCode() { return staffOwnerCode; }
    public String getStaffOwnerName() { return staffOwnerName; }
    public String getStaffOwnerTel() { return staffOwnerTel; }
    public String getStatus() { return status; }
    public Long getStockNum() { return stockNum; }
    public Long getStockNumImp() { return stockNumImp; }
    public String getStreet() { return street; }
    public Long getSubOwnerId() { return subOwnerId; }
    public Long getSubOwnerType() { return subOwnerType; }
    public String getTel() { return tel; }
    public String getTin() { return tin; }
    public String getTtnsCode() { return ttnsCode; }
    public Long getType() { return type; }
    public Long getUserId() { return userId; }
    public String getWarningContent() { return warningContent; }
    public String getShopCode() { return shopCode; }
    public String getShopName() { return shopName; }
    public boolean getIsPointOfSale() { return isPointOfSale; }
    public Long getShopChanelTypeId() { return shopChanelTypeId; }
    public String getTablePk() { return tablePk; }
    public String getIpAddress() { return ipAddress; }
    public Long getSaleTransStaffId() { return saleTransStaffId; }
    public String getShopPath() { return shopPath; }
    public String getShopProvince() { return shopProvince; }
    public String getShopPrecinct() { return shopPrecinct; }
    public String getChannelTypeName() { return channelTypeName; }
    public String getManagerName() { return managerName; }
    public String getShopType() { return shopType; }
    public String getAction() { return action; }
    public String getProgramName() { return programName; }
    public String getCurrentURL() { return currentURL; }
    public String getSdnNumber() { return sdnNumber; }
    public Boolean getShopIsAgent() { return shopIsAgent; }
    public Long getGroupChannelTypeId() { return groupChannelTypeId; }
    public String getPositionName() { return positionName; }
    public String getBankplusBankCode() { return bankplusBankCode; }
    public String getParentBankCode() { return parentBankCode; }
    public String getCitadCode() { return citadCode; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountOwner() { return accountOwner; }
    public Long getMapShopBankCitadId() { return mapShopBankCitadId; }
    public Long getTenantId() { return tenantId; }
    public String getPricePolicyName() { return pricePolicyName; }
    public String getDiscountPolicyName() { return discountPolicyName; }
    public String getProvinceName() { return provinceName; }
    public String getMapAreaChainChannel() { return mapAreaChainChannel; }
    public Long getSubChannelTypeId() { return subChannelTypeId; }
    public boolean isVsaRequest() { return vsaRequest; }
    public boolean getIsWalletRegister() { return isWalletRegister; }
    public String getDistrictName() { return districtName; }
    public String getPrecinctName() { return precinctName; }
    public byte[] getData() { return data; }
    public boolean isCheckMapGroupPos() { return checkMapGroupPos; }
    public List<Long> getChannelList() { return channelList; }
    public String getPositionGroup() { return positionGroup; }
    public String getPositionGroupName() { return positionGroupName; }
    public String getCheck() { return check; }
    public String getCreateUser() { return createUser; }
    public String getUpdateUser() { return updateUser; }
    public Date getCreateDatetime() { return createDatetime; }
    public Date getUpdateDatetime() { return updateDatetime; }
    public String getOrganizationId() { return organizationId; }
    public Long getParentShopId() { return parentShopId; }
    public String getBrandName() { return brandName; }
    public Long getLv() { return lv; }
    public String getParenBank() { return parenBank; }
    public String getAddressIdNo() { return addressIdNo; }
    public String getBusinessType() { return businessType; }
    public String getTotalRecords() { return totalRecords; }
    public String getCompanyTax() { return companyTax; }
    public Long getTaxType() { return taxType; }
    public String getCompanyBusinessNo() { return companyBusinessNo; }
    public String getCompanyRepresentPosition() { return companyRepresentPosition; }
    public Long getRowNum() { return rowNum; }
    public String getChannelCode() { return channelCode; }
    public String getStockCode() { return stockCode; }
    public String getIdentityCard() { return identityCard; }
    public Date getIssueDate() { return issueDate; }
    public String getIssuePlace() { return issuePlace; }
    public String getPhoneNumber() { return phoneNumber; }
    public List<String> getRoleIds() { return roleIds; }
    public String getFullName() { return fullName; }
    public Long getChannelTypeOfStaff() { return channelTypeOfStaff; }
    public String getNameOfChannelStaff() { return nameOfChannelStaff; }
    public Long getChannelTypeOfShop() { return channelTypeOfShop; }
    public String getNameOfChannelShop() { return nameOfChannelShop; }
    public String getCompanyPresent() { return companyPresent; }
    public String getActiveAddress() { return activeAddress; }
    public String getBirthDay() { return birthDay; }
    public String getIdIssueDateAI() { return idIssueDateAI; }
    public String getFileIdentifyId() { return fileIdentifyId; }
    public String getFilePortraitId() { return filePortraitId; }
    public String getFileIdentifyIdUp() { return fileIdentifyIdUp; }
    public String getFileIdentifyIdUn() { return fileIdentifyIdUn; }
    public String getDescription() { return description; }
    public Date getEffectDatetime() { return effectDatetime; }
    public Date getExpireDatetime() { return expireDatetime; }
    public String getUsername() { return username; }
    public String getInstallationStatus() { return installationStatus; }
    public boolean getNeedCheckAllRole() { return needCheckAllRole; }
    public boolean isOwner() { return owner; }
    public String getSapMessage() { return sapMessage; }
    public boolean isSendMessage() { return sendMessage; }
    public List<String> getLstAreaCode() { return lstAreaCode; }
    public String getShopProvinceName() { return shopProvinceName; }
    public String getStaffInfoType() { return staffInfoType; }
    public Boolean getMyViettelOrder() { return myViettelOrder; }
    public String getUpdateNote() { return updateNote; }
    public boolean isDisable() { return disable; }
    public Date getCompanyBusinessDate() { return companyBusinessDate; }
    public String getCompanyBusinessPlace() { return companyBusinessPlace; }
    public String getOrganizationName() { return organizationName; }
    public String getPositionCode() { return positionCode; }

    // --- Setters ---

    public void setStatusName(String statusName) { this.statusName = statusName; }
    public void setBankplusMobile(String bankplusMobile) { this.bankplusMobile = bankplusMobile; }
    public void setBirthday(Date birthday) { this.birthday = birthday; }
    public void setBtsCode(String btsCode) { this.btsCode = btsCode; }
    public void setBusinessLicence(String businessLicence) { this.businessLicence = businessLicence; }
    public void setBusinessMethod(Long businessMethod) { this.businessMethod = businessMethod; }
    public void setChannelTypeId(Long channelTypeId) { this.channelTypeId = channelTypeId; }
    public void setContractFromDate(Date contractFromDate) { this.contractFromDate = contractFromDate; }
    public void setContractMethod(Long contractMethod) { this.contractMethod = contractMethod; }
    public void setContractNo(String contractNo) { this.contractNo = contractNo; }
    public void setIdentifyAccountNo(String identifyAccountNo) { this.identifyAccountNo = identifyAccountNo; }
    public void setIdentifyAccountName(String identifyAccountName) { this.identifyAccountName = identifyAccountName; }
    public void setApprovalType(String approvalType) { this.approvalType = approvalType; }
    public void setContractToDate(Date contractToDate) { this.contractToDate = contractToDate; }
    public void setDepositValue(Long depositValue) { this.depositValue = depositValue; }
    public void setDiscountPolicy(String discountPolicy) { this.discountPolicy = discountPolicy; }
    public void setDistrict(String district) { this.district = district; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setGender(Long gender) { this.gender = gender; }
    public void setHasEquipment(Long hasEquipment) { this.hasEquipment = hasEquipment; }
    public void setHasTin(Long hasTin) { this.hasTin = hasTin; }
    public void setIdIssueDate(Date idIssueDate) { this.idIssueDate = idIssueDate; }
    public void setIdIssuePlace(String idIssuePlace) { this.idIssuePlace = idIssuePlace; }
    public void setIdNo(String idNo) { this.idNo = idNo; }
    public void setIsNew(String isNew) { this.isNew = isNew; }
    public void setIdType(Long idType) { this.idType = idType; }
    public void setImsi(String imsi) { this.imsi = imsi; }
    public void setIsdn(String isdn) { this.isdn = isdn; }
    public void setLastLockTime(Date lastLockTime) { this.lastLockTime = lastLockTime; }
    public void setLastModified(Date lastModified) { this.lastModified = lastModified; }
    public void setLockFlag(String lockFlag) { this.lockFlag = lockFlag; }
    public void setLockStatus(Long lockStatus) { this.lockStatus = lockStatus; }
    public void setName(String name) { this.name = name; }
    public void setNote(String note) { this.note = note; }
    public void setPaymentLimit(Long paymentLimit) { this.paymentLimit = paymentLimit; }
    public void setPaymentUsage(Long paymentUsage) { this.paymentUsage = paymentUsage; }
    public void setPin(String pin) { this.pin = pin; }
    public void setPointOfSale(String pointOfSale) { this.pointOfSale = pointOfSale; }
    public void setPointOfSaleType(Long pointOfSaleType) { this.pointOfSaleType = pointOfSaleType; }
    public void setPricePolicy(String pricePolicy) { this.pricePolicy = pricePolicy; }
    public void setProvince(String province) { this.province = province; }
    public void setAreaCode(String areaCode) { this.areaCode = areaCode; }
    public void setStreetBlock(String streetBlock) { this.streetBlock = streetBlock; }
    public void setHome(String home) { this.home = home; }
    public void setPrecinct(String precinct) { this.precinct = precinct; }
    public void setRegisterInfo(String registerInfo) { this.registerInfo = registerInfo; }
    public void setSerial(String serial) { this.serial = serial; }
    public void setShopId(Long shopId) { this.shopId = shopId; }
    public void setShopOwnerId(Long shopOwnerId) { this.shopOwnerId = shopOwnerId; }
    public void setStaffCode(String staffCode) { this.staffCode = staffCode; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    public void setStaffOwnType(String staffOwnType) { this.staffOwnType = staffOwnType; }
    public void setStaffOwnerId(Long staffOwnerId) { this.staffOwnerId = staffOwnerId; }
    public void setStaffOwnerCode(String staffOwnerCode) { this.staffOwnerCode = staffOwnerCode; }
    public void setStaffOwnerName(String staffOwnerName) { this.staffOwnerName = staffOwnerName; }
    public void setStaffOwnerTel(String staffOwnerTel) { this.staffOwnerTel = staffOwnerTel; }
    public void setStatus(String status) { this.status = status; }
    public void setStockNum(Long stockNum) { this.stockNum = stockNum; }
    public void setStockNumImp(Long stockNumImp) { this.stockNumImp = stockNumImp; }
    public void setStreet(String street) { this.street = street; }
    public void setSubOwnerId(Long subOwnerId) { this.subOwnerId = subOwnerId; }
    public void setSubOwnerType(Long subOwnerType) { this.subOwnerType = subOwnerType; }
    public void setTel(String tel) { this.tel = tel; }
    public void setTin(String tin) { this.tin = tin; }
    public void setTtnsCode(String ttnsCode) { this.ttnsCode = ttnsCode; }
    public void setType(Long type) { this.type = type; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setWarningContent(String warningContent) { this.warningContent = warningContent; }
    public void setShopCode(String shopCode) { this.shopCode = shopCode; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public void setIsPointOfSale(boolean isPointOfSale) { this.isPointOfSale = isPointOfSale; }
    public void setShopChanelTypeId(Long shopChanelTypeId) { this.shopChanelTypeId = shopChanelTypeId; }
    public void setTablePk(String tablePk) { this.tablePk = tablePk; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public void setSaleTransStaffId(Long saleTransStaffId) { this.saleTransStaffId = saleTransStaffId; }
    public void setShopPath(String shopPath) { this.shopPath = shopPath; }
    public void setShopProvince(String shopProvince) { this.shopProvince = shopProvince; }
    public void setShopPrecinct(String shopPrecinct) { this.shopPrecinct = shopPrecinct; }
    public void setChannelTypeName(String channelTypeName) { this.channelTypeName = channelTypeName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public void setShopType(String shopType) { this.shopType = shopType; }
    public void setAction(String action) { this.action = action; }
    public void setProgramName(String programName) { this.programName = programName; }
    public void setCurrentURL(String currentURL) { this.currentURL = currentURL; }
    public void setSdnNumber(String sdnNumber) { this.sdnNumber = sdnNumber; }
    public void setShopIsAgent(Boolean shopIsAgent) { this.shopIsAgent = shopIsAgent; }
    public void setGroupChannelTypeId(Long groupChannelTypeId) { this.groupChannelTypeId = groupChannelTypeId; }
    public void setPositionName(String positionName) { this.positionName = positionName; }
    public void setBankplusBankCode(String bankplusBankCode) { this.bankplusBankCode = bankplusBankCode; }
    public void setParentBankCode(String parentBankCode) { this.parentBankCode = parentBankCode; }
    public void setCitadCode(String citadCode) { this.citadCode = citadCode; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    public void setAccountOwner(String accountOwner) { this.accountOwner = accountOwner; }
    public void setMapShopBankCitadId(Long mapShopBankCitadId) { this.mapShopBankCitadId = mapShopBankCitadId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
    public void setPricePolicyName(String pricePolicyName) { this.pricePolicyName = pricePolicyName; }
    public void setDiscountPolicyName(String discountPolicyName) { this.discountPolicyName = discountPolicyName; }
    public void setProvinceName(String provinceName) { this.provinceName = provinceName; }
    public void setMapAreaChainChannel(String mapAreaChainChannel) { this.mapAreaChainChannel = mapAreaChainChannel; }
    public void setSubChannelTypeId(Long subChannelTypeId) { this.subChannelTypeId = subChannelTypeId; }
    public void setVsaRequest(boolean vsaRequest) { this.vsaRequest = vsaRequest; }
    public void setIsWalletRegister(boolean walletRegister) { isWalletRegister = walletRegister; }
    public void setDistrictName(String districtName) { this.districtName = districtName; }
    public void setPrecinctName(String precinctName) { this.precinctName = precinctName; }
    public void setData(byte[] data) { this.data = data; }
    public void setCheckMapGroupPos(boolean checkMapGroupPos) { this.checkMapGroupPos = checkMapGroupPos; }
    public void setChannelList(List<Long> channelList) { this.channelList = channelList; }
    public void setPositionGroup(String positionGroup) { this.positionGroup = positionGroup; }
    public void setPositionGroupName(String positionGroupName) { this.positionGroupName = positionGroupName; }
    public void setCheck(String check) { this.check = check; }
    public void setCreateUser(String createUser) { this.createUser = createUser; }
    public void setUpdateUser(String updateUser) { this.updateUser = updateUser; }
    public void setCreateDatetime(Date createDatetime) { this.createDatetime = createDatetime; }
    public void setUpdateDatetime(Date updateDatetime) { this.updateDatetime = updateDatetime; }
    public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }
    public void setParentShopId(Long parentShopId) { this.parentShopId = parentShopId; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    public void setLv(Long lv) { this.lv = lv; }
    public void setParenBank(String parenBank) { this.parenBank = parenBank; }
    public void setAddressIdNo(String addressIdNo) { this.addressIdNo = addressIdNo; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public void setTotalRecords(String totalRecords) { this.totalRecords = totalRecords; }
    public void setCompanyTax(String companyTax) { this.companyTax = companyTax; }
    public void setTaxType(Long taxType) { this.taxType = taxType; }
    public void setCompanyBusinessNo(String companyBusinessNo) { this.companyBusinessNo = companyBusinessNo; }
    public void setCompanyRepresentPosition(String companyRepresentPosition) { this.companyRepresentPosition = companyRepresentPosition; }
    public void setRowNum(Long rowNum) { this.rowNum = rowNum; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    public void setStockCode(String stockCode) { this.stockCode = stockCode; }
    public void setIdentityCard(String identityCard) { this.identityCard = identityCard; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
    public void setIssuePlace(String issuePlace) { this.issuePlace = issuePlace; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public void setRoleIds(List<String> roleIds) { this.roleIds = roleIds; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setChannelTypeOfStaff(Long channelTypeOfStaff) { this.channelTypeOfStaff = channelTypeOfStaff; }
    public void setNameOfChannelStaff(String nameOfChannelStaff) { this.nameOfChannelStaff = nameOfChannelStaff; }
    public void setChannelTypeOfShop(Long channelTypeOfShop) { this.channelTypeOfShop = channelTypeOfShop; }
    public void setNameOfChannelShop(String nameOfChannelShop) { this.nameOfChannelShop = nameOfChannelShop; }
    public void setCompanyPresent(String companyPresent) { this.companyPresent = companyPresent; }
    public void setActiveAddress(String activeAddress) { this.activeAddress = activeAddress; }
    public void setBirthDay(String birthDay) { this.birthDay = birthDay; }
    public void setIdIssueDateAI(String idIssueDateAI) { this.idIssueDateAI = idIssueDateAI; }
    public void setFileIdentifyId(String fileIdentifyId) { this.fileIdentifyId = fileIdentifyId; }
    public void setFilePortraitId(String filePortraitId) { this.filePortraitId = filePortraitId; }
    public void setFileIdentifyIdUp(String fileIdentifyIdUp) { this.fileIdentifyIdUp = fileIdentifyIdUp; }
    public void setFileIdentifyIdUn(String fileIdentifyIdUn) { this.fileIdentifyIdUn = fileIdentifyIdUn; }
    public void setDescription(String description) { this.description = description; }
    public void setEffectDatetime(Date effectDatetime) { this.effectDatetime = effectDatetime; }
    public void setExpireDatetime(Date expireDatetime) { this.expireDatetime = expireDatetime; }
    public void setUsername(String username) { this.username = username; }
    public void setInstallationStatus(String installationStatus) { this.installationStatus = installationStatus; }
    public void setNeedCheckAllRole(boolean needCheckAllRole) { this.needCheckAllRole = needCheckAllRole; }
    public void setOwner(boolean owner) { this.owner = owner; }
    public void setSapMessage(String sapMessage) { this.sapMessage = sapMessage; }
    public void setSendMessage(boolean sendMessage) { this.sendMessage = sendMessage; }
    public void setLstAreaCode(List<String> lstAreaCode) { this.lstAreaCode = lstAreaCode; }
    public void setShopProvinceName(String shopProvinceName) { this.shopProvinceName = shopProvinceName; }
    public void setStaffInfoType(String staffInfoType) { this.staffInfoType = staffInfoType; }
    public void setMyViettelOrder(Boolean myViettelOrder) { this.myViettelOrder = myViettelOrder; }
    public void setUpdateNote(String updateNote) { this.updateNote = updateNote; }
    public void setDisable(boolean disable) { this.disable = disable; }
    public void setCompanyBusinessDate(Date companyBusinessDate) { this.companyBusinessDate = companyBusinessDate; }
    public void setCompanyBusinessPlace(String companyBusinessPlace) { this.companyBusinessPlace = companyBusinessPlace; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }
    public void setPositionCode(String positionCode) { this.positionCode = positionCode; }
    public com.viettel.bccs.organization.shop.dto.ShopDTO getShop() { return shop; }
    public void setShop(com.viettel.bccs.organization.shop.dto.ShopDTO shop) { this.shop = shop; }
    public Boolean getIsChannelOfSalePoint() { return isChannelOfSalePoint; }
    public void setIsChannelOfSalePoint(Boolean isChannelOfSalePoint) { this.isChannelOfSalePoint = isChannelOfSalePoint; }

    @Override
    public String toString() {
        return "" + staffId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StaffDTO staffDTO = (StaffDTO) o;
        if (staffId != null ? !staffId.equals(staffDTO.staffId) : staffDTO.staffId != null) return false;
        if (staffCode != null ? !staffCode.equals(staffDTO.staffCode) : staffDTO.staffCode != null) return false;
        return name != null ? name.equals(staffDTO.name) : staffDTO.name == null;
    }

    @Override
    public int hashCode() {
        int result = staffId != null ? staffId.hashCode() : 0;
        result = 31 * result + (staffCode != null ? staffCode.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
