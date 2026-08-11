package com.viettel.bccs.productcatalog.productpackage.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.viettel.bccs.productcatalog.prodpackproductoffertype.dto.response.ProdPackProductOfferTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Mô hình dịch vụ bán hàng nâng cao")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SaleServiceModelAdvanceDTO implements Serializable {

    @Schema(description = "Dịch vụ bán hàng")
    private ProdPackProductOfferTypeDTO saleServiceModel;

    @Schema(description = "Danh sách chi tiết dịch vụ bán hàng")
    @Builder.Default
    @Size(max = 500, message = "listSaleServiceDetail tối đa 500 phần tử")
    private List<PackageOfferDTO> listSaleServiceDetail = new ArrayList<>();
}