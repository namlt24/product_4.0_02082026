package com.viettel.bccs.productcatalog.prodpackproductoffertype.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.viettel.bccs.productcatalog.client.dto.ShopDTO;
import com.viettel.bccs.productcatalog.prodpackshop.dto.response.ProdPackShopDTO;
import com.viettel.bccs.productcatalog.productpackage.dto.response.PackageOfferDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của PROD_PACK_PRODUCT_OFFER_TYPE
 * (xem ProdPackProductOfferTypeEntity) — @Size/@Pattern/@Min/@Max chỉ áp dụng khi giá trị khác null.
 * productOfferTypeName lấy theo độ dài cột NAME của PRODUCT_OFFER_TYPE; shopId/shopCode lấy theo
 * ShopDTO (cross-service, không có ràng buộc độ dài cột nên dùng bound mặc định).
 */
@Schema(description = "Liên kết gói sản phẩm và loại mặt hàng")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProdPackProductOfferTypeDTO implements Serializable {

    @Schema(description = "ID liên kết")
    @Min(value = 1, message = "prodPackTypeId phải >= 1")
    @Max(value = 9999999999L, message = "prodPackTypeId vượt quá độ dài cột (precision 10)")
    private Long prodPackTypeId;

    @Schema(description = "ID gói sản phẩm")
    @Min(value = 1, message = "productPackageId phải >= 1")
    @Max(value = 9999999999L, message = "productPackageId vượt quá độ dài cột (precision 10)")
    private Long productPackageId;

    @Schema(description = "ID loại mặt hàng")
    @Min(value = 1, message = "productOfferTypeId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferTypeId vượt quá độ dài cột (precision 10)")
    private Long productOfferTypeId;

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "status chỉ gồm chữ hoặc số")
    private String status;

    @Schema(description = "Cờ cập nhật tồn kho")
    @Size(max = 1, message = "updateStock đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "updateStock chỉ gồm chữ hoặc số")
    private String updateStock;

    @Schema(description = "Cờ kiểm tra tồn kho nhân viên")
    @Size(max = 1, message = "checkStaffStock đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "checkStaffStock chỉ gồm chữ hoặc số")
    private String checkStaffStock;

    @Schema(description = "Cờ kiểm tra tồn kho cửa hàng")
    @Size(max = 1, message = "checkShopStock đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "checkShopStock chỉ gồm chữ hoặc số")
    private String checkShopStock;

    @Schema(description = "Cờ bắt buộc")
    @Size(max = 1, message = "require đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "require chỉ gồm chữ hoặc số")
    private String require;

    @Schema(description = "Mô tả")
    @Size(max = 200, message = "description tối đa 200 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,200}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Schema(description = "Giới hạn hàng hóa")
    @Min(value = 0, message = "limitGoods phải >= 0")
    @Max(value = 9999999999L, message = "limitGoods vượt quá độ dài cột (precision 10)")
    private Long limitGoods;

    @Schema(description = "Phân phối")
    @Min(value = 0, message = "distribute phải >= 0")
    @Max(value = 99L, message = "distribute vượt quá độ dài cột (precision 2)")
    private Long distribute;

    @Schema(description = "Chuyển IM")
    @Size(max = 1, message = "transferIm đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "transferIm chỉ gồm chữ hoặc số")
    private String transferIm;

    @Schema(description = "Người tạo")
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Schema(description = "Ngày tạo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createDatetime;

    @Schema(description = "Người cập nhật")
    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Schema(description = "Ngày cập nhật")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date updateDatetime;

    @Schema(description = "Tên loại mặt hàng")
    @Size(max = 50, message = "productOfferTypeName tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "productOfferTypeName không được chứa ký tự điều khiển")
    private String productOfferTypeName;

    @Schema(description = "ID cửa hàng của nhân viên (chỉ set khi checkShopStock=\"1\" và có truyền staffCode)")
    @Min(value = 1, message = "shopId phải >= 1")
    @Max(value = 9999999999L, message = "shopId vượt quá độ dài cột (precision 10)")
    private Long shopId;

    @Schema(description = "Mã cửa hàng của nhân viên (chỉ set khi checkShopStock=\"1\" và có truyền staffCode)")
    @Size(max = 50, message = "shopCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "shopCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopCode;

    @Schema(description = "Danh sách kho chức năng")
    @Size(max = 1000, message = "specShopList tối đa 1000 phần tử")
    private List<ShopDTO> specShopList;

    @Schema(description = "Danh sách các kho chức năng của mặt hàng")
    @Size(max = 1000, message = "prodPackShopDTOs tối đa 1000 phần tử")
    private List<ProdPackShopDTO> prodPackShopDTOs = new ArrayList<>();//Danh sach cac kho chuc nang cua mat hang

    @Schema(description = "Danh sách mặt hàng đi kèm")
    @Size(max = 1000, message = "packageOfferDTOs tối đa 1000 phần tử")
    private List<PackageOfferDTO> packageOfferDTOs = new ArrayList<>(); // danh sach mat hang di kem

}