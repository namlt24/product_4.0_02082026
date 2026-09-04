package com.viettel.bccs.productcatalog.productoffercharuse.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 50, message = "extensionType tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "extensionType chỉ gồm chữ, số, '_' hoặc '-'")
    private String extensionType;

    @Schema(description = "Giá trị mở rộng", example = "M100")
    @Size(max = 100, message = "extensionValue tối đa 100 ký tự")
    private String extensionValue;
}
