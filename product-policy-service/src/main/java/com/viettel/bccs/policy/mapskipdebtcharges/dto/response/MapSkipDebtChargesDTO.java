package com.viettel.bccs.policy.mapskipdebtcharges.dto.response;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của MAP_SKIP_DEBT_CHARGES
 * (xem MapSkipDebtChargesEntity) — @Size/@Pattern/@Min/@Max chỉ áp dụng khi giá trị khác null.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapSkipDebtChargesDTO {

    public static enum Columns {
        ID, TEL_SERVICE_ID, PRODUCT_CODE, PRODUCT_NAME, REG_REASON_ID, REASON_NAME,
        CHANNEL_TYPE_ID, CHANNEL_NAME, PROVINCE_CODE, PROVINCE_NAME, DISTRICT_CODE, DISTRICT_NAME,
        PRECINCT_CODE, PRECINCT_NAME, SHOP_CODE, STAFF_CODE, ACTION_CODE, ACTION_NAME, STATUS,
        CREATE_USER, CREATE_DATETIME, UPDATE_USER, UPDATE_DATETIME, CYCLE, SKIP_HOT_CHARGES, PAY_TYPE,
        EFFECT_DATE, END_DATE, OFFER_ID, CUST_GROUP_ID, CUST_TYPE, CUST_ID_NO, CUST_ACCOUNT_NO,
        CUST_CODE, ACT_STATUS, SKIP_LAST_SUB, SKIP_CONTRACT
    }

    @Schema(description = "ID (PK)", example = "1")
    @Min(value = 0, message = "id phải >= 0")
    @Max(value = 9999999999L, message = "id vượt quá độ dài cột (precision 10)")
    private Long id;

    @Schema(description = "ID dịch vụ viễn thông", example = "1")
    @Min(value = 0, message = "telServiceId phải >= 0")
    @Max(value = 9999999999L, message = "telServiceId vượt quá độ dài cột (precision 10)")
    private Long telServiceId;

    @Schema(description = "Mã gói cước", example = "POBAS")
    @Size(max = 100, message = "productCode tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String productCode;

    @Schema(description = "Tên gói cước")
    @Size(max = 100, message = "productName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "productName không được chứa ký tự điều khiển")
    private String productName;

    @Schema(description = "ID lý do đăng ký", example = "1")
    @Min(value = 0, message = "regReasonId phải >= 0")
    @Max(value = 9999999999L, message = "regReasonId vượt quá độ dài cho phép")
    private Long regReasonId;

    @Schema(description = "Tên lý do đăng ký")
    @Size(max = 100, message = "reasonName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "reasonName không được chứa ký tự điều khiển")
    private String reasonName;

    @Schema(description = "ID loại kênh", example = "1")
    @Min(value = 0, message = "channelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cho phép")
    private Long channelTypeId;

    @Schema(description = "Tên kênh")
    @Size(max = 100, message = "channelName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "channelName không được chứa ký tự điều khiển")
    private String channelName;

    @Schema(description = "Mã tỉnh/thành phố", example = "HN")
    @Size(max = 100, message = "provinceCode tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "provinceCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String provinceCode;

    @Schema(description = "Tên tỉnh/thành phố")
    @Size(max = 100, message = "provinceName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "provinceName không được chứa ký tự điều khiển")
    private String provinceName;

    @Schema(description = "Mã quận/huyện")
    @Size(max = 100, message = "districtCode tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "districtCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String districtCode;

    @Schema(description = "Tên quận/huyện")
    @Size(max = 100, message = "districtName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "districtName không được chứa ký tự điều khiển")
    private String districtName;

    @Schema(description = "Mã phường/xã")
    @Size(max = 100, message = "precinctCode tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "precinctCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String precinctCode;

    @Schema(description = "Tên phường/xã")
    @Size(max = 100, message = "precinctName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "precinctName không được chứa ký tự điều khiển")
    private String precinctName;

    @Schema(description = "Mã cửa hàng")
    @Size(max = 100, message = "shopCode tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "shopCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopCode;

    @Schema(description = "Mã nhân viên")
    @Size(max = 100, message = "staffCode tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String staffCode;

    @Schema(description = "Mã hành động", example = "00")
    @Size(max = 100, message = "actionCode tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "actionCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String actionCode;

    @Schema(description = "Tên hành động")
    @Size(max = 100, message = "actionName tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "actionName không được chứa ký tự điều khiển")
    private String actionName;

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "status chỉ gồm chữ hoặc số")
    private String status;

    @Schema(description = "Người tạo", example = "system")
    @Size(max = 100, message = "createUser tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,100}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Thời điểm tạo")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "system")
    @Size(max = 100, message = "updateUser tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,100}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "Thời điểm cập nhật")
    private Date updateDatetime;

    @Schema(description = "Chu kỳ", example = "1")
    @Min(value = 0, message = "cycle phải >= 0")
    @Max(value = 9999999999L, message = "cycle vượt quá độ dài cột (precision 10)")
    private Long cycle;

    @Schema(description = "Bỏ qua phí nóng (Y/N)", example = "Y")
    @Size(max = 2, message = "skipHotCharges tối đa 2 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "skipHotCharges chỉ gồm chữ hoặc số")
    private String skipHotCharges;

    @Schema(description = "Hình thức thanh toán", example = "1")
    @Size(max = 2, message = "payType tối đa 2 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "payType chỉ gồm chữ hoặc số")
    private String payType;

    @Schema(description = "Ngày hiệu lực")
    private Date effectDate;

    @Schema(description = "Ngày kết thúc")
    private Date endDate;

    @Schema(description = "ID mặt hàng", example = "1")
    @Min(value = 0, message = "offerId phải >= 0")
    @Max(value = 9999999999L, message = "offerId vượt quá độ dài cho phép")
    private Long offerId;

    @Schema(description = "ID nhóm khách hàng", example = "1")
    @Min(value = 0, message = "custGroupId phải >= 0")
    @Max(value = 9, message = "custGroupId vượt quá độ dài cột (precision 1)")
    private Long custGroupId;

    @Schema(description = "Loại khách hàng")
    @Size(max = 10, message = "custType tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "custType chỉ gồm chữ, số, '_' hoặc '-'")
    private String custType;

    @Schema(description = "Số giấy tờ khách hàng (CMND/CCCD...)")
    @Size(max = 50, message = "custIdNo tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "custIdNo không được chứa ký tự điều khiển")
    private String custIdNo;

    @Schema(description = "Số tài khoản khách hàng")
    @Size(max = 100, message = "custAccountNo tối đa 100 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "custAccountNo không được chứa ký tự điều khiển")
    private String custAccountNo;

    @Schema(description = "Mã khách hàng")
    @Size(max = 100, message = "custCode tối đa 100 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "custCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String custCode;

    @Schema(description = "Trạng thái thuê bao")
    @Size(max = 3, message = "actStatus tối đa 3 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,3}$", message = "actStatus chỉ gồm chữ hoặc số")
    private String actStatus;

    @Schema(description = "Bỏ qua thuê bao cuối (Y/N)")
    @Size(max = 2, message = "skipLastSub tối đa 2 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "skipLastSub chỉ gồm chữ hoặc số")
    private String skipLastSub;

    @Schema(description = "Bỏ qua hợp đồng (Y/N)")
    @Size(max = 2, message = "skipContract tối đa 2 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "skipContract chỉ gồm chữ hoặc số")
    private String skipContract;
}
