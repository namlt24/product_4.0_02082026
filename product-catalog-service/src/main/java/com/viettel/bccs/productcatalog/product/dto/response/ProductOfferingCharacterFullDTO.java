package com.viettel.bccs.productcatalog.product.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharValueDTO;
import io.swagger.v3.oas.annotations.media.Schema;
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
    private Long productOfferingId;

    @Schema(description = "Mã sản phẩm (product_offering.code)", example = "DATA3G90")
    private String productCode;

    @Schema(description = "Đặc tính sản phẩm")
    private ProductSpecCharDTO productSpecCharDTO;

    @Schema(description = "Giá trị của đặc tính sản phẩm")
    private ProductSpecCharValueDTO productSpecCharValueDTO;

    @Schema(description = "Loại mở rộng", example = "VAS")
    private String extensionType;

    @Schema(description = "Giá trị mở rộng", example = "M100")
    private String extensionValue;

    @Schema(description = "Danh sách mã VAS xung đột", example = "[\"VAS_A\", \"VAS_B\"]")
    private List<String> vasCodesConflict = new ArrayList<>();

}
