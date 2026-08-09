package com.viettel.bccs.productcatalog.productoffertype.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Schema(description = "Loại mặt hàng")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProductOfferTypeDTO implements Serializable {

    @Schema(description = "ID loại mặt hàng")
    private Long productOfferTypeId;

    @Schema(description = "ID loại mặt hàng cha")
    private Long parentId;

    @Schema(description = "Tên loại mặt hàng", example = "Sim trả trước")
    private String name;

    @Schema(description = "Mô tả")
    private String description;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

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
}