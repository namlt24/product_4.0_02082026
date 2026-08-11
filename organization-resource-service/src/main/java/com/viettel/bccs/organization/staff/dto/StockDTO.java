package com.viettel.bccs.organization.staff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO tổng hợp (không có bảng STOCK trong DB), bound suy luận hợp lý theo ngữ cảnh:
 * code/type tương tự các trường mã ngắn khác trong module (~40 ký tự), name tương tự trường tên (~300 ký tự).
 */
@Schema
@Data
public class StockDTO {
    @Schema(description = "ID kho", example = "1")
    @Min(value = 0, message = "stockId phải >= 0")
    @Max(value = 9999999999L, message = "stockId vượt quá giới hạn cho phép")
    private Long stockId;

    @Schema(description = "Mã kho", example = "STOCK_001")
    @Size(max = 40, message = "code tối đa 40 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "code chỉ gồm chữ, số, '_' hoặc '-'")
    private String code;

    @Schema(description = "Tên kho", example = "Kho hàng hóa Hà Nội")
    @Size(max = 300, message = "name tối đa 300 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,300}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Loại kho", example = "1")
    @Size(max = 20, message = "type tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "type chỉ gồm chữ, số, '_' hoặc '-'")
    private String type;

    public StockDTO() {
    }

    public StockDTO(Long stockId, String code, String name, String type) {
        this.stockId = stockId;
        this.code = code;
        this.name = name;
        this.type = type;
    }
}
