package com.viettel.bccs.productcatalog.productpackage.dto.response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Cửa hàng trong gói sản phẩm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProdPackShopDTO implements Serializable {

    // === ID fields ===
    @Schema(description = "ID cửa hàng trong gói")
    @Min(value = 1, message = "prodPackShopId phải >= 1")
    @Max(value = 9999999999L, message = "prodPackShopId vượt quá độ dài cho phép")
    private Long prodPackShopId;

    @Schema(description = "ID loại sản phẩm trong gói")
    @Min(value = 1, message = "prodPackTypeId phải >= 1")
    @Max(value = 9999999999L, message = "prodPackTypeId vượt quá độ dài cho phép")
    private Long prodPackTypeId;

    @Schema(description = "ID cửa hàng")
    @Min(value = 1, message = "shopId phải >= 1")
    @Max(value = 9999999999L, message = "shopId vượt quá độ dài cho phép")
    private Long shopId;

    @Schema(description = "ID loại sản phẩm")
    @Min(value = 1, message = "productOfferTypeId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferTypeId vượt quá độ dài cho phép")
    private Long productOfferTypeId;

    // === Basic info ===
    @Size(max = 50, message = "shopCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "shopCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String shopCode;

    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Size(max = 50, message = "productPackageCode tối đa 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]{0,50}$", message = "productPackageCode chỉ gồm chữ, số hoặc '_'")
    private String productPackageCode;

    // === Date fields ===
    @Schema(description = "Ngày tạo")
    private Date createDatetime;

    @Schema(description = "Ngày cập nhật")
    private Date updateDatetime;

    // === User ===
    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    // === Exclude list ===
    @Builder.Default
    @Size(max = 500, message = "excluseIdList tối đa 500 phần tử")
    private List<Long> excluseIdList = new ArrayList<>();
}