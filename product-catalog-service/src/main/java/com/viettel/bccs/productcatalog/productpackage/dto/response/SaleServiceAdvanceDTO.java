package com.viettel.bccs.productcatalog.productpackage.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.viettel.bccs.productcatalog.client.dto.ShopDTO;
import com.viettel.bccs.productcatalog.common.dto.BaseMessage;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productpackagefee.dto.response.ProductPackageFeeDTO;
import com.viettel.bccs.productcatalog.prodpackproductoffertype.dto.response.ProdPackProductOfferTypeDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Schema(description = "Thông tin dịch vụ bán hàng nâng cao")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SaleServiceAdvanceDTO extends BaseMessage implements Serializable {

    @Schema(description = "Dịch vụ bán hàng")
    private ProductPackageDTO saleService;

    @Schema(description = "Danh sách mô hình dịch vụ bán hàng")
    @Builder.Default
    private List<SaleServiceModelAdvanceDTO> listSaleServiceModel = new ArrayList<>();

    @Schema(description = "Danh sách phí dịch vụ bán hàng")
    @Builder.Default
    private List<ProductPackageFeeDTO> listSaleServicePrice = new ArrayList<>();

    @Schema(description = "Danh sách thuộc tính sản phẩm")
    @Builder.Default
    private List<ProductSpecCharDTO> lstProductSpecCharDTO = new ArrayList<>();

    @Schema(description = "Danh sách loại mặt hàng của dịch vụ bán hàng")
    @Builder.Default
    private List<ProdPackProductOfferTypeDTO> listProductOfferType = new ArrayList<>();

    @Schema(description = "Cờ TLV")
    @Builder.Default
    private boolean isTLV = false;

    @Schema(description = "Cờ thưởng")
    @Builder.Default
    private boolean isBonus = false;

    @Schema(description = "Cờ xóa gói")
    @Builder.Default
    private boolean removePackage = false;

    @Schema(description = "Danh sách ID cửa hàng liên kết với các loại mặt hàng của dịch vụ bán hàng")
    @Builder.Default
    private List<Long> shopIds = new ArrayList<>();

    @Schema(description = "Danh sách thông tin cửa hàng active liên kết với dịch vụ bán hàng")
    @Builder.Default
    private List<ShopDTO> specShopList = new ArrayList<>();

    @Schema(description = "Map prodPackTypeId -> danh sách shopId tương ứng")
    @Builder.Default
    private Map<Long, List<Long>> prodPackTypeIdToShopIds = Map.of();
}