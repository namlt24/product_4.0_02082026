package com.viettel.bccs.policy.mapping.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Date;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài/precision cột thật của MAPPING
 * (xem MappingEntity).
 */
public record MappingResponse(

        @Schema(description = "Id mapping (PK)", example = "1")
        @Min(value = 0, message = "id phải >= 0")
        @Max(value = 9999999999L, message = "id vượt quá độ dài cột (precision 10)")
        Long id,

        @Schema(description = "Mã VAS", example = "VAS001")
        @Size(max = 10, message = "vas tối đa 10 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,10}$", message = "vas chỉ gồm chữ và số")
        String vas,

        @Schema(description = "Tên VAS", example = "Dịch vụ giá trị gia tăng")
        @Size(max = 100, message = "vasName tối đa 100 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "vasName không được chứa ký tự điều khiển")
        String vasName,

        @Schema(description = "Tên sản phẩm", example = "Internet cáp quang")
        @Size(max = 100, message = "productName tối đa 100 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "productName không được chứa ký tự điều khiển")
        String productName,

        @Schema(description = "Mã sản phẩm", example = "PROD001")
        @Size(max = 15, message = "productCode tối đa 15 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,15}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
        String productCode,

        @Schema(description = "Tên hành động", example = "Đấu nối mới")
        @Size(max = 100, message = "actionName tối đa 100 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "actionName không được chứa ký tự điều khiển")
        String actionName,

        @Schema(description = "Mã hành động", example = "5001")
        @Size(max = 30, message = "actionCode tối đa 30 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,30}$", message = "actionCode chỉ gồm chữ, số, '_' hoặc '-'")
        String actionCode,

        @Schema(description = "Id lý do", example = "1")
        @Min(value = 0, message = "reasonId phải >= 0")
        @Max(value = 9999999999L, message = "reasonId vượt quá độ dài cột (precision 10)")
        Long reasonId,

        @Schema(description = "Tên lý do", example = "Đấu nối mới thuê bao")
        @Size(max = 100, message = "reasonName tối đa 100 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "reasonName không được chứa ký tự điều khiển")
        String reasonName,

        @Schema(description = "Id dịch vụ viễn thông", example = "100")
        @Min(value = 0, message = "telServiceId phải >= 0")
        @Max(value = 9999999999L, message = "telServiceId vượt quá độ dài cột (precision 10)")
        Long telServiceId,

        @Schema(description = "Id dịch vụ bán hàng", example = "200")
        @Min(value = 0, message = "saleServiceId phải >= 0")
        @Max(value = 9999999999L, message = "saleServiceId vượt quá độ dài cột (precision 10)")
        Long saleServiceId,

        @Schema(description = "Tên dịch vụ bán hàng", example = "Gói cước A")
        @Size(max = 100, message = "saleServiceName tối đa 100 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "saleServiceName không được chứa ký tự điều khiển")
        String saleServiceName,

        @Schema(description = "Mã dịch vụ bán hàng", example = "SS001")
        @Size(max = 20, message = "saleServiceCode tối đa 20 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "saleServiceCode chỉ gồm chữ, số, '_' hoặc '-'")
        String saleServiceCode,

        @Schema(description = "Kênh", example = "1")
        @Size(max = 1, message = "channel tối đa 1 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "channel chỉ gồm chữ và số")
        String channel,

        @Schema(description = "Trạng thái (0/1)", example = "1")
        @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
        @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
        String status,

        @Schema(description = "Người tạo", example = "system")
        @Size(max = 30, message = "userCreate tối đa 30 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,30}$", message = "userCreate chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String userCreate,

        @Schema(description = "Người cập nhật", example = "system")
        @Size(max = 30, message = "userUpdate tối đa 30 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,30}$", message = "userUpdate chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String userUpdate,

        @Schema(description = "Thời điểm tạo")
        Date createDatetime,

        @Schema(description = "Thời điểm thay đổi")
        Date changeDatetime,

        @Schema(description = "Địa chỉ IP", example = "192.168.1.1")
        @Size(max = 50, message = "ip tối đa 50 ký tự")
        @Pattern(regexp = "^[0-9a-fA-F:.]{0,50}$", message = "ip không đúng định dạng IPv4/IPv6")
        String ip,

        @Schema(description = "Ngày kết thúc hiệu lực")
        Date endEffectDate,

        @Schema(description = "Loại mapping", example = "1")
        @Size(max = 1, message = "typeMapping tối đa 1 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "typeMapping chỉ gồm chữ và số")
        String typeMapping,

        @Schema(description = "Id hành động", example = "5001")
        @Size(max = 30, message = "actionId tối đa 30 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,30}$", message = "actionId chỉ gồm chữ, số, '_' hoặc '-'")
        String actionId
) {
}
