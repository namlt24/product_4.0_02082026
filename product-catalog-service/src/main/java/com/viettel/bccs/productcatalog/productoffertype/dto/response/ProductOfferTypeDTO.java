package com.viettel.bccs.productcatalog.productoffertype.dto.response;

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
 * Bound/pattern trên từng field lấy đúng theo độ dài cột thật của PRODUCT_OFFER_TYPE
 * (xem ProductOfferTypeEntity) — @Size/@Pattern/@Min/@Max chỉ áp dụng khi giá trị khác null.
 */
@Schema(description = "Loại mặt hàng")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProductOfferTypeDTO implements Serializable {

    @Schema(description = "ID loại mặt hàng")
    @Min(value = 1, message = "productOfferTypeId phải >= 1")
    @Max(value = 9999999999L, message = "productOfferTypeId vượt quá độ dài cột (precision 10)")
    private Long productOfferTypeId;

    @Schema(description = "ID loại mặt hàng cha")
    @Min(value = 1, message = "parentId phải >= 1")
    @Max(value = 9999999999L, message = "parentId vượt quá độ dài cột (precision 10)")
    private Long parentId;

    @Schema(description = "Tên loại mặt hàng", example = "Sim trả trước")
    @Size(max = 50, message = "name tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Mô tả")
    @Size(max = 2000, message = "description tối đa 2000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,2000}$", message = "description không được chứa ký tự điều khiển")
    private String description;

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