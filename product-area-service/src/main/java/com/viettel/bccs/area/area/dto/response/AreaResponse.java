package com.viettel.bccs.area.area.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của AREA (xem AreaEntity) —
 * field không nullable ở DB vẫn cho phép null ở response record (record component không có
 * @NotNull), @Size/@Pattern chỉ áp dụng khi giá trị khác null.
 */
public record AreaResponse(

        @Schema(description = "Mã địa bàn (PK)", example = "A076003005001")
        @Size(min = 1, max = 200, message = "areaCode tối đa 200 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{1,200}$", message = "areaCode chỉ gồm chữ và số")
        String areaCode,

        @Schema(description = "Mã địa bàn cha", example = "A076003005")
        @Size(max = 200, message = "parentCode tối đa 200 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,200}$", message = "parentCode chỉ gồm chữ và số")
        String parentCode,

        @Schema(description = "Nhóm địa bàn", example = "01")
        @Size(max = 5, message = "areaGroup tối đa 5 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,5}$", message = "areaGroup chỉ gồm chữ, số, '_' hoặc '-'")
        String areaGroup,

        @Schema(description = "Mã tỉnh/thành", example = "A076")
        @Size(max = 50, message = "province tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "province chỉ gồm chữ và số")
        String province,

        @Schema(description = "Mã quận/huyện", example = "A076003")
        @Size(max = 50, message = "district tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "district chỉ gồm chữ và số")
        String district,

        @Schema(description = "Mã phường/xã", example = "A076003005")
        @Size(max = 50, message = "precinct tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "precinct chỉ gồm chữ và số")
        String precinct,

        @Schema(description = "Mã tổ/khối phố", example = "001")
        @Size(max = 50, message = "streetBlock tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "streetBlock chỉ gồm chữ và số")
        String streetBlock,

        @Schema(description = "Tên địa bàn", example = "Cau Giay")
        @Size(max = 100, message = "name tối đa 100 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "name không được chứa ký tự điều khiển")
        String name,

        @Schema(description = "Tên đầy đủ của địa bàn", example = "Quan Cau Giay")
        @Size(max = 400, message = "fullName tối đa 400 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,400}$", message = "fullName không được chứa ký tự điều khiển")
        String fullName,

        @Schema(description = "Cờ trung tâm (0/1)", example = "1")
        @Size(min = 1, max = 1, message = "center đúng 1 ký tự")
        @Pattern(regexp = "^[01]$", message = "center chỉ nhận giá trị 0 hoặc 1")
        String center,

        @Schema(description = "Mã tổng đài PSTN", example = "024")
        @Size(max = 20, message = "pstnCode tối đa 20 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,20}$", message = "pstnCode chỉ gồm chữ và số")
        String pstnCode,

        @Schema(description = "Mã tỉnh/thành (viễn thông)", example = "A076")
        @Size(max = 50, message = "provinceCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "provinceCode chỉ gồm chữ và số")
        String provinceCode,

        @Schema(description = "Trạng thái (0/1)", example = "1")
        @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
        @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
        String status,

        @Schema(description = "Người tạo", example = "system")
        @Size(max = 50, message = "createUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String createUser,

        @Schema(description = "Thời điểm tạo")
        Date createDatetime,

        @Schema(description = "Người cập nhật", example = "system")
        @Size(max = 50, message = "updateUser tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
        String updateUser,

        @Schema(description = "Thời điểm cập nhật")
        Date updateDatetime,

        @Schema(description = "Id vùng miền", example = "1")
        @DecimalMin(value = "0", message = "regionId phải >= 0")
        @DecimalMax(value = "99999999999999999999", message = "regionId vượt quá độ dài cột (precision 20)")
        BigDecimal regionId,

        @Schema(description = "Mã địa bàn trên VT Map", example = "HN01")
        @Size(max = 20, message = "vtMapCode tối đa 20 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,20}$", message = "vtMapCode chỉ gồm chữ và số")
        String vtMapCode,

        @Schema(description = "Diện tích (km2)", example = "100")
        @DecimalMin(value = "0", message = "square phải >= 0")
        @DecimalMax(value = "99999999999999999999", message = "square vượt quá độ dài cột (precision 20)")
        BigDecimal square,

        @Schema(description = "Dân số", example = "1000000")
        @DecimalMin(value = "0", message = "population phải >= 0")
        @DecimalMax(value = "99999999999999999999", message = "population vượt quá độ dài cột (precision 20)")
        BigDecimal population,

        @Schema(description = "Số hộ gia đình", example = "300000")
        @DecimalMin(value = "0", message = "households phải >= 0")
        @DecimalMax(value = "99999999999999999999", message = "households vượt quá độ dài cột (precision 20)")
        BigDecimal households,

        @Schema(description = "Loại địa bàn", example = "URBAN")
        @Size(max = 10, message = "areaType tối đa 10 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "areaType chỉ gồm chữ, số, '_' hoặc '-'")
        String areaType,

        @Schema(description = "Mã địa bàn theo chuẩn Việt Nam", example = "001")
        @Size(max = 50, message = "vnCode tối đa 50 ký tự")
        @Pattern(regexp = "^[A-Za-z0-9]{0,50}$", message = "vnCode chỉ gồm chữ và số")
        String vnCode,

        @Schema(description = "Tên địa bàn theo chuẩn Việt Nam", example = "Hà Nội")
        @Size(max = 100, message = "vnName tối đa 100 ký tự")
        @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100}$", message = "vnName không được chứa ký tự điều khiển")
        String vnName,

        @Schema(description = "Cờ đánh dấu địa bàn mới (0/1)", example = "0")
        @Size(min = 1, max = 1, message = "isNew đúng 1 ký tự")
        @Pattern(regexp = "^[01]$", message = "isNew chỉ nhận giá trị 0 hoặc 1")
        String isNew
) {
}
