package com.viettel.bccs.productcatalog.prodpackshop.dto.response;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
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

/**
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của PROD_PACK_SHOP
 * (xem ProdPackShopEntity) — @Size/@Pattern/@Min/@Max chỉ áp dụng khi giá trị khác null.
 */
@Schema(description = "Liên kết gói sản phẩm và cửa hàng")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProdPackShopDTO implements Serializable {

    @Schema(description = "ID liên kết")
    @Min(value = 1, message = "prodPackShopId phải >= 1")
    @Max(value = 9999999999L, message = "prodPackShopId vượt quá độ dài cột (precision 10)")
    private Long prodPackShopId;

    @Schema(description = "ID cửa hàng")
    @Min(value = 1, message = "shopId phải >= 1")
    @Max(value = 9999999999L, message = "shopId vượt quá độ dài cột (precision 10)")
    private Long shopId;

    @Schema(description = "ID liên kết gói sản phẩm - loại mặt hàng")
    @Min(value = 1, message = "prodPackTypeId phải >= 1")
    @Max(value = 9999999999L, message = "prodPackTypeId vượt quá độ dài cột (precision 10)")
    private Long prodPackTypeId;

    @Schema(description = "Trạng thái", example = "1")
    @Size(max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "status chỉ gồm chữ hoặc số")
    private String status;

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
}