package com.viettel.bccs.productcatalog.prodpackshop.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Schema(description = "Liên kết gói sản phẩm và cửa hàng")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProdPackShopDTO implements Serializable {

    @Schema(description = "ID liên kết")
    private Long prodPackShopId;

    @Schema(description = "ID cửa hàng")
    private Long shopId;

    @Schema(description = "ID liên kết gói sản phẩm - loại mặt hàng")
    private Long prodPackTypeId;

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