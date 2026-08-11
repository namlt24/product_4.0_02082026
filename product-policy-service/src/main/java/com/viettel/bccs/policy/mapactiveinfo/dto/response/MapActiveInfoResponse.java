package com.viettel.bccs.policy.mapactiveinfo.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Date;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của MAP_ACTIVE_INFO
 * (xem MapActiveInfoEntity) — field không nullable ở DB vẫn cho phép null ở response
 * record (record component không có @NotNull), @Size/@Pattern chỉ áp dụng khi giá trị khác null.
 */
public record MapActiveInfoResponse(

        @Schema(description = "ID", example = "1")
        @Min(value = 0, message = "id phải >= 0")
        @Max(value = 9999999999L, message = "id vượt quá độ dài cột (precision 10)")
        Long id,

        @Schema(description = "ID dịch vụ viễn thông", example = "73")
        @Min(value = 0, message = "telServiceId phải >= 0")
        @Max(value = 9999999999L, message = "telServiceId vượt quá độ dài cột (precision 10)")
        Long telServiceId,

        @Schema(description = "Mã sản phẩm", example = "PROD001")
        @Size(max = 50, message = "productCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
        String productCode,

        @Schema(description = "Tên sản phẩm", example = "Dịch vụ Mobifone")
        @Size(max = 100, message = "productName tối đa 100 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "productName không được chứa ký tự điều khiển")
        String productName,

        @Schema(description = "ID lý do đăng ký", example = "1")
        @Min(value = 0, message = "regReasonId phải >= 0")
        @Max(value = 9999999999L, message = "regReasonId vượt quá độ dài cột (precision 10)")
        Long regReasonId,

        @Schema(description = "Tên lý do", example = "Lý do đăng ký mới")
        @Size(max = 100, message = "reasonName tối đa 100 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "reasonName không được chứa ký tự điều khiển")
        String reasonName,

        @Schema(description = "Mã khuyến mãi", example = "PROMO001")
        @Size(max = 50, message = "promCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "promCode chỉ gồm chữ, số, '_' hoặc '-'")
        String promCode,

        @Schema(description = "Tên khuyến mãi", example = "Khuyến mãi tháng 1")
        @Size(max = 150, message = "promName tối đa 150 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,150}$", message = "promName không được chứa ký tự điều khiển")
        String promName,

        @Schema(description = "ID loại kênh", example = "1")
        @Min(value = 0, message = "channelTypeId phải >= 0")
        @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
        Long channelTypeId,

        @Schema(description = "Tên kênh", example = "Kênh 1")
        @Size(max = 50, message = "channelName tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "channelName không được chứa ký tự điều khiển")
        String channelName,

        @Schema(description = "Mã tỉnh/thành phố", example = "HN")
        @Size(max = 50, message = "provinceCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "provinceCode chỉ gồm chữ, số, '_' hoặc '-'")
        String provinceCode,

        @Schema(description = "Mã quận/huyện", example = "HN001")
        @Size(max = 50, message = "districtCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "districtCode chỉ gồm chữ, số, '_' hoặc '-'")
        String districtCode,

        @Schema(description = "Ngày hiệu lực", example = "2024-01-01")
        Date effectDate,

        @Schema(description = "Ngày hết hiệu lực", example = "2025-12-31")
        Date endDate,

        @Schema(description = "Tên tỉnh/thành phố", example = "Hà Nội")
        @Size(max = 50, message = "provinceName tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "provinceName không được chứa ký tự điều khiển")
        String provinceName,

        @Schema(description = "Tên quận/huyện", example = "Hoàn Kiếm")
        @Size(max = 50, message = "districtName tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "districtName không được chứa ký tự điều khiển")
        String districtName,

        @Schema(description = "ID sản phẩm", example = "1")
        @Min(value = 0, message = "offerId phải >= 0")
        @Max(value = 9999999999L, message = "offerId vượt quá độ dài cột (precision 10)")
        Long offerId,

        @Schema(description = "Tên sản phẩm", example = "Gói cước Mobifone 50")
        @Size(max = 100, message = "offerName tối đa 100 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "offerName không được chứa ký tự điều khiển")
        String offerName,

        @Schema(description = "Trạng thái", example = "1")
        @Min(value = 0, message = "status phải >= 0")
        @Max(value = 99, message = "status vượt quá độ dài cột (precision 2)")
        Long status,

        @Schema(description = "Tên phường/xã", example = "Phường Lý Thái Tổ")
        @Size(max = 50, message = "precinctName tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "precinctName không được chứa ký tự điều khiển")
        String precinctName,

        @Schema(description = "Mã phường/xã", example = "HN00101")
        @Size(max = 50, message = "precinctCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "precinctCode chỉ gồm chữ, số, '_' hoặc '-'")
        String precinctCode,

        @Schema(description = "Mã cửa hàng", example = "SHOP001")
        @Size(max = 50, message = "shopCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "shopCode chỉ gồm chữ, số, '_' hoặc '-'")
        String shopCode,

        @Schema(description = "Mã nhân viên", example = "STAFF001")
        @Size(max = 50, message = "staffCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
        String staffCode,

        @Schema(description = "Mã hành động", example = "00")
        @Size(max = 10, message = "actionCode tối đa 10 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "actionCode chỉ gồm chữ, số, '_' hoặc '-'")
        String actionCode,

        @Schema(description = "Tên hành động", example = "Kích hoạt")
        @Size(max = 50, message = "actionName tối đa 50 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "actionName không được chứa ký tự điều khiển")
        String actionName,

        @Schema(description = "Số lượng giới hạn", example = "100")
        @Size(max = 10, message = "limitNumber tối đa 10 ký tự")
        @Pattern(regexp = "^[0-9]{0,10}$", message = "limitNumber chỉ gồm chữ số")
        String limitNumber,

        @Schema(description = "Yêu cầu CAPTCHA", example = "1")
        @Min(value = 0, message = "captcharRequire phải >= 0")
        @Max(value = 9, message = "captcharRequire vượt quá độ dài cột (precision 1)")
        Long captcharRequire,

        @Schema(description = "Đơn vị", example = "tháng")
        @Size(max = 2, message = "unit tối đa 2 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,2}$", message = "unit chỉ gồm chữ và số")
        String unit,

        @Schema(description = "Nhóm khách hàng", example = "VIP")
        @Size(max = 10, message = "customerGroup tối đa 10 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "customerGroup chỉ gồm chữ, số, '_' hoặc '-'")
        String customerGroup,

        @Schema(description = "Loại khách hàng", example = "Individual")
        @Size(max = 10, message = "customerType tối đa 10 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "customerType chỉ gồm chữ, số, '_' hoặc '-'")
        String customerType,

        @Schema(description = "Loại phụ", example = "SUB_TYPE")
        @Size(max = 4, message = "subType tối đa 4 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,4}$", message = "subType chỉ gồm chữ, số, '_' hoặc '-'")
        String subType,

        @Schema(description = "Nhóm phụ", example = "SUB_GRP")
        @Size(max = 10, message = "subGroup tối đa 10 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "subGroup chỉ gồm chữ, số, '_' hoặc '-'")
        String subGroup,

        @Schema(description = "Tài liệu chính sách", example = "POL001")
        @Size(max = 100, message = "policyDoc tối đa 100 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,100}$", message = "policyDoc chỉ gồm chữ, số, '_' hoặc '-'")
        String policyDoc,

        @Schema(description = "Nhóm hành động", example = "GROUP_A")
        @Size(max = 50, message = "actionGroup tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "actionGroup chỉ gồm chữ, số, '_' hoặc '-'")
        String actionGroup,

        @Schema(description = "Tên nhóm hành động", example = "Nhóm A")
        @Size(max = 200, message = "actionGroupName tối đa 200 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "actionGroupName không được chứa ký tự điều khiển")
        String actionGroupName,

        @Schema(description = "Tên file đính kèm", example = "policy.pdf")
        @Size(max = 200, message = "fileName tối đa 200 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "fileName không được chứa ký tự điều khiển")
        String fileName,

        @Schema(description = "ID trạm", example = "1")
        @Min(value = 0, message = "stationId phải >= 0")
        @Max(value = 9999999999L, message = "stationId vượt quá độ dài cột (precision 10)")
        Long stationId,

        @Schema(description = "ID cửa hàng", example = "1")
        @Min(value = 0, message = "shopId phải >= 0")
        @Max(value = 9999999999L, message = "shopId vượt quá độ dài cột (precision 10)")
        Long shopId,

        @Schema(description = "Người tạo", example = "admin")
        @Size(max = 50, message = "createUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String createUser,

        @Schema(description = "Ngày phát hành", example = "2024-01-01")
        Date issueDatetime,

        @Schema(description = "Mã trạm", example = "ST001")
        @Size(max = 1000, message = "stationCodes tối đa 1000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "stationCodes không được chứa ký tự điều khiển")
        String stationCodes,

        @Schema(description = "Loại thanh toán", example = "1")
        @Size(max = 1, message = "payType đúng 1 ký tự")
        @Pattern(regexp = "^[0-9]{0,1}$", message = "payType chỉ nhận 1 chữ số")
        String payType,

        @Schema(description = "Công nghệ", example = "4G")
        @Size(max = 10, message = "technology tối đa 10 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "technology chỉ gồm chữ, số, '_' hoặc '-'")
        String technology,

        @Schema(description = "Ngày cập nhật")
        Date updateDatetime,

        @Schema(description = "Người cập nhật", example = "admin")
        @Size(max = 50, message = "updateUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String updateUser,

        @Schema(description = "Mã nhóm khu vực", example = "AREA_GRP")
        @Size(max = 50, message = "areaGroupCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "areaGroupCode chỉ gồm chữ, số, '_' hoặc '-'")
        String areaGroupCode,

        @Schema(description = "Mã VAS", example = "VAS001")
        @Size(max = 50, message = "vasCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "vasCode chỉ gồm chữ, số, '_' hoặc '-'")
        String vasCode,

        @Schema(description = "Tên VAS", example = "Dịch vụ VAS")
        @Size(max = 100, message = "vasName tối đa 100 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "vasName không được chứa ký tự điều khiển")
        String vasName,

        @Schema(description = "Mã node", example = "NODE001")
        @Size(max = 1500, message = "nodeCode tối đa 1500 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1500}$", message = "nodeCode không được chứa ký tự điều khiển")
        String nodeCode,

        @Schema(description = "Ghi chú", example = "Ghi chú thông tin")
        @Size(max = 3000, message = "note tối đa 3000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,3000}$", message = "note không được chứa ký tự điều khiển")
        String note,

        @Schema(description = "Mã nhóm node", example = "GRP_NODE")
        @Size(max = 50, message = "groupNodeCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "groupNodeCode chỉ gồm chữ, số, '_' hoặc '-'")
        String groupNodeCode,

        @Schema(description = "Loại nhân viên", example = "STAFF")
        @Size(max = 5, message = "staffType tối đa 5 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,5}$", message = "staffType chỉ gồm chữ, số, '_' hoặc '-'")
        String staffType,

        @Schema(description = "Số kinh doanh", example = "BUS001")
        @Size(max = 4000, message = "businessNo tối đa 4000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "businessNo không được chứa ký tự điều khiển")
        String businessNo,

        @Schema(description = "Mã nhóm phụ", example = "SUB_GRP_CODE")
        @Size(max = 4000, message = "subGroupCode tối đa 4000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "subGroupCode không được chứa ký tự điều khiển")
        String subGroupCode,

        @Schema(description = "Nhóm khách hàng SME", example = "SME_GRP")
        @Size(max = 4000, message = "smeCustomerGroup tối đa 4000 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "smeCustomerGroup không được chứa ký tự điều khiển")
        String smeCustomerGroup,

        @Schema(description = "ID offline", example = "123")
        @Min(value = 0, message = "importOfflineId phải >= 0")
        @Max(value = 9999999999L, message = "importOfflineId vượt quá độ dài cột (precision 10)")
        Long importOfflineId,

        @Schema(description = "Phương thức kết nối", example = "1")
        @Min(value = 0, message = "connectMethod phải >= 0")
        @Max(value = 99, message = "connectMethod phải <= 99")
        Long connectMethod,

        @Schema(description = "ID dịch vụ viễn thông đính kèm", example = "2")
        @Min(value = 0, message = "attachTelServiceId phải >= 0")
        @Max(value = 9999999999L, message = "attachTelServiceId vượt quá độ dài cột (precision 10)")
        Long attachTelServiceId,

        @Schema(description = "Mã sản phẩm đính kèm", example = "PROD002")
        @Size(max = 50, message = "attachProductCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "attachProductCode chỉ gồm chữ, số, '_' hoặc '-'")
        String attachProductCode,

        @Schema(description = "Đơn/gói (single/combo)", example = "1")
        @Min(value = 0, message = "singleOrCombo phải >= 0")
        @Max(value = 9, message = "singleOrCombo phải <= 9")
        Long singleOrCombo,

        @Schema(description = "Mã sản phẩm cũ", example = "PROD_OLD")
        @Size(max = 50, message = "oldProductCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "oldProductCode chỉ gồm chữ, số, '_' hoặc '-'")
        String oldProductCode,

        @Schema(description = "Mã dự án", example = "PRJ001")
        @Size(max = 50, message = "projectCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "projectCode chỉ gồm chữ, số, '_' hoặc '-'")
        String projectCode
) {
}
