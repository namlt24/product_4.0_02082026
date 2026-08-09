package com.viettel.bccs.productcatalog.prodpackproductoffertype.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.viettel.bccs.productcatalog.client.dto.ShopDTO;
import com.viettel.bccs.productcatalog.prodpackshop.dto.response.ProdPackShopDTO;
import com.viettel.bccs.productcatalog.productpackage.dto.response.PackageOfferDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Schema(description = "Liên kết gói sản phẩm và loại mặt hàng")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProdPackProductOfferTypeDTO implements Serializable {

    @Schema(description = "ID liên kết")
    private Long prodPackTypeId;

    @Schema(description = "ID gói sản phẩm")
    private Long productPackageId;

    @Schema(description = "ID loại mặt hàng")
    private Long productOfferTypeId;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Cờ cập nhật tồn kho")
    private String updateStock;

    @Schema(description = "Cờ kiểm tra tồn kho nhân viên")
    private String checkStaffStock;

    @Schema(description = "Cờ kiểm tra tồn kho cửa hàng")
    private String checkShopStock;

    @Schema(description = "Cờ bắt buộc")
    private String require;

    @Schema(description = "Mô tả")
    private String description;

    @Schema(description = "Giới hạn hàng hóa")
    private Long limitGoods;

    @Schema(description = "Phân phối")
    private Long distribute;

    @Schema(description = "Chuyển IM")
    private String transferIm;

    @Schema(description = "Người tạo")
    private String createUser;

    @Schema(description = "Ngày tạo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createDatetime;

    @Schema(description = "Người cập nhật")
    private String updateUser;

    @Schema(description = "Ngày cập nhật")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date updateDatetime;

    @Schema(description = "Tên loại mặt hàng")
    private String productOfferTypeName;

    @Schema(description = "Danh sách kho chức năng")
    private List<ShopDTO> specShopList;

    private List<ProdPackShopDTO> prodPackShopDTOs = new ArrayList<>();//Danh sach cac kho chuc nang cua mat hang
    private List<PackageOfferDTO> packageOfferDTOs = new ArrayList<>(); // danh sach mat hang di kem

}