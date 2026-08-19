package com.viettel.bccs.productcatalog.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharValueDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "Thông tin sản phẩm gói cước kèm đặc tính và giá trị đặc tính")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProductOfferingCharacterFullDTO {

    @Schema(description = "ID sản phẩm (product_offering)", example = "111")
    @Min(value = 0, message = "productOfferingId phải >= 0")
    @Max(value = 9999999999L, message = "productOfferingId vượt quá độ dài cột (precision 10)")
    private Long productOfferingId;

    @Schema(description = "Mã sản phẩm (product_offering.code)", example = "DATA3G90")
    @Size(max = 50, message = "productCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String productCode;

    @Schema(description = "Đặc tính sản phẩm")
    private ProductSpecCharDTO productSpecCharDTO;

    @Schema(description = "Giá trị của đặc tính sản phẩm")
    private ProductSpecCharValueDTO productSpecCharValueDTO;

    @Schema(description = "Loại mở rộng", example = "VAS")
    @Size(max = 50, message = "extensionType tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "extensionType chỉ gồm chữ, số, '_' hoặc '-'")
    private String extensionType;

    @Schema(description = "Giá trị mở rộng", example = "M100")
    @Size(max = 100, message = "extensionValue tối đa 100 ký tự")
    private String extensionValue;

    @Schema(description = "Danh sách mã VAS xung đột", example = "[\"VAS_A\", \"VAS_B\"]")
    @Size(max = 1000, message = "vasCodesConflict tối đa 1000 phần tử")
    private List<String> vasCodesConflict = new ArrayList<>();

}
