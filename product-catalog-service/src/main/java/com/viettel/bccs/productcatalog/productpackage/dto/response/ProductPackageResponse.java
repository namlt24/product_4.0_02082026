package com.viettel.bccs.productcatalog.productpackage.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Schema(description = "Phản hồi thông tin gói sản phẩm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProductPackageResponse implements Serializable {

    @Schema(description = "ID gói sản phẩm")
    private Long productPackageId;

    @Schema(description = "Mã gói sản phẩm")
    private String code;

    @Schema(description = "Tên gói sản phẩm")
    private String name;

    @Schema(description = "Mô tả")
    private String description;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Loại gói sản phẩm")
    private String type;

    @Schema(description = "Loại bán hàng")
    private String saleType;

    @Schema(description = "Đơn vị")
    private String unit;

    @Schema(description = "Ngày hiệu lực")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date expireDatetime;

    @Schema(description = "Ngày tạo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date createDatetime;

    @Schema(description = "Ngày cập nhật")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Ho_Chi_Minh")
    private Date updateDatetime;

    private String createUser;
    private String updateUser;
    private String version;

    @Schema(description = "ID kế toán")
    private Long accountingId;

    private String feeType;

    @Schema(description = "ID dịch vụ viễn thông")
    private Long telecomServiceId;

    private String note;
    private String note1;
    private String note2;

    private Long areaGroupId;
    private Long ownerShopId;
    private Long sapMaterialNumber;
    private String itTelcol;
}