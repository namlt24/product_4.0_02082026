package com.viettel.bccs.productcatalog.productpackagecharuse.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductPackageCharUseDTO implements Serializable {

    @Schema(description = "ID thuộc tính sản phẩm trong gói")
    private Long productPackageCharUseId;

    @Schema(description = "ID gói sản phẩm")
    private Long productPackageId;

    @Schema(description = "ID thuộc tính sản phẩm")
    private Long productSpecCharId;

    @Schema(description = "ID giá trị thuộc tính sản phẩm")
    private Long productSpecCharValueId;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Giá trị cụ thể")
    private String specificValue;

    @Schema(description = "Giới hạn")
    private Long limited;

    @Schema(description = "Mô tả")
    private String description;

    @Schema(description = "Ngày tạo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createDatetime;

    @Schema(description = "Ngày hiệu lực")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date expireDatetime;

    @Schema(description = "Ngày cập nhật")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date updateDatetime;

    private String createUser;
    private String updateUser;
}