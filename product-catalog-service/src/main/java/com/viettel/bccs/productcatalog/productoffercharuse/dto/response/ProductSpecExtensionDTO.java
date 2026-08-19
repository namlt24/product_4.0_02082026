package com.viettel.bccs.productcatalog.productoffercharuse.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Thông tin mở rộng của đặc tính price plan (parse từ specific_value)")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductSpecExtensionDTO {

    @Schema(description = "Loại mở rộng", example = "VAS")
    private String extensionType;

    @Schema(description = "Giá trị mở rộng", example = "M100")
    private String extensionValue;
}
