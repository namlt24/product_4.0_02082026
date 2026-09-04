package com.viettel.bccs.productcatalog.product.dto.response;

import java.util.List;

import com.viettel.bccs.productcatalog.productoffertype.dto.response.ProductOfferTypeDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * 1 nhóm loại hàng hoá trong kết quả API getListStockTypeWS: loại mặt hàng (productOfferType)
 * kèm danh sách mặt hàng thuộc loại đó (bước 6 + 7 + 9 của flow).
 */
@Schema(description = "Loại hàng hoá kèm danh sách mặt hàng")
public record ProductOfferTypeStockDTO(

        @Schema(description = "Loại mặt hàng (name = \"Mặt hàng\" nếu productOfferTypeId = 7)")
        ProductOfferTypeDTO productOfferType,

        @Schema(description = "Danh sách mặt hàng thuộc loại này, kèm giá")
        @Size(max = 1000, message = "productOfferings tối đa 1000 phần tử")
        List<ProductOfferingStockDTO> productOfferings
) {
}
