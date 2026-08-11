package com.viettel.bccs.productcatalog.productoffercharuse.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpecCharValueDTO {

    @Schema(description = "ID giá trị đặc tính sản phẩm", example = "12345")
    @Min(value = 1, message = "productSpecCharValueId phải >= 1")
    @Max(value = 9999999999L, message = "productSpecCharValueId vượt quá độ dài cột (precision 10)")
    private Long productSpecCharValueId;

    @Schema(description = "ID đặc tính sản phẩm", example = "67890")
    @Min(value = 1, message = "productSpecCharId phải >= 1")
    @Max(value = 9999999999L, message = "productSpecCharId vượt quá độ dài cột (precision 10)")
    private Long productSpecCharId;

    @Schema(description = "Loại giá trị", example = "TEXT")
    @Size(max = 10, message = "valueType tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "valueType chỉ gồm chữ, số, '_' hoặc '-'")
    private String valueType;

    @Schema(description = "Mặc định", example = "1")
    @Min(value = 0, message = "isDefault phải >= 0")
    @Max(value = 9, message = "isDefault vượt quá độ dài cột (precision 1)")
    private Long isDefault;

    @Schema(description = "Giá trị", example = "VAL")
    @Size(max = 4000, message = "value tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "value không được chứa ký tự điều khiển")
    private String value;

    @Schema(description = "Đơn vị tính", example = "GB")
    @Size(max = 10, message = "unitOfMeasure tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "unitOfMeasure chỉ gồm chữ, số, '_' hoặc '-'")
    private String unitOfMeasure;

    @Schema(description = "Giá trị từ", example = "0")
    @Size(max = 4000, message = "valueFrom tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "valueFrom không được chứa ký tự điều khiển")
    private String valueFrom;

    @Schema(description = "Giá trị đến", example = "100")
    @Size(max = 4000, message = "valueTo tối đa 4000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,4000}$", message = "valueTo không được chứa ký tự điều khiển")
    private String valueTo;

    @Schema(description = "Khoảng cách", example = "10")
    @Size(max = 10, message = "rangeInterval tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,10}$", message = "rangeInterval chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String rangeInterval;

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "status chỉ gồm chữ hoặc số")
    private String status;

    @Schema(description = "Tên giá trị", example = "Tên giá trị")
    @Size(max = 500, message = "name tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Giá trị đặc biệt", example = "SPECIFIC")
    @Size(max = 100000, message = "specificValue tối đa 100000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,100000}$", message = "specificValue không được chứa ký tự điều khiển")
    private String specificValue;

    @Schema(description = "Ghi chú", example = "Ghi chú giá trị")
    @Size(max = 256, message = "note tối đa 256 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,256}$", message = "note không được chứa ký tự điều khiển")
    private String note;
}
