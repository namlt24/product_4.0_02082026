package com.viettel.bccs.productcatalog.productoffercharuse.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private Long productSpecCharValueId;

    @Schema(description = "ID đặc tính sản phẩm", example = "67890")
    private Long productSpecCharId;

    @Schema(description = "Loại giá trị", example = "TEXT")
    private String valueType;

    @Schema(description = "Mặc định", example = "1")
    private Long isDefault;

    @Schema(description = "Giá trị", example = "VAL")
    private String value;

    @Schema(description = "Đơn vị tính", example = "GB")
    private String unitOfMeasure;

    @Schema(description = "Giá trị từ", example = "0")
    private String valueFrom;

    @Schema(description = "Giá trị đến", example = "100")
    private String valueTo;

    @Schema(description = "Khoảng cách", example = "10")
    private String rangeInterval;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Tên giá trị", example = "Tên giá trị")
    private String name;

    @Schema(description = "Giá trị đặc biệt", example = "SPECIFIC")
    private String specificValue;

    @Schema(description = "Ghi chú", example = "Ghi chú giá trị")
    private String note;
}