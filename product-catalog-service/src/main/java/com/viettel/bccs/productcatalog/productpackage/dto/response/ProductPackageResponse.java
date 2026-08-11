package com.viettel.bccs.productcatalog.productpackage.dto.response;

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
    @Min(value = 1, message = "productPackageId phải >= 1")
    @Max(value = 9999999999L, message = "productPackageId vượt quá độ dài cột (precision 10)")
    private Long productPackageId;

    @Schema(description = "Mã gói sản phẩm")
    @Size(max = 50, message = "code tối đa 50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]{0,50}$", message = "code chỉ gồm chữ, số hoặc '_'")
    private String code;

    @Schema(description = "Tên gói sản phẩm")
    @Size(max = 500, message = "name tối đa 500 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,500}$", message = "name không được chứa ký tự điều khiển")
    private String name;

    @Schema(description = "Mô tả")
    @Size(max = 512, message = "description tối đa 512 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,512}$", message = "description không được chứa ký tự điều khiển")
    private String description;

    @Schema(description = "Trạng thái", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Loại gói sản phẩm")
    @Size(min = 1, max = 1, message = "type đúng 1 ký tự")
    @Pattern(regexp = "^[12]$", message = "type chỉ nhận giá trị 1 (hàng hoá) hoặc 2 (dịch vụ bán hàng)")
    private String type;

    @Schema(description = "Loại bán hàng")
    @Size(min = 1, max = 1, message = "saleType đúng 1 ký tự")
    @Pattern(regexp = "^[0-9]$", message = "saleType chỉ nhận 1 chữ số")
    private String saleType;

    @Schema(description = "Đơn vị")
    @Size(max = 10, message = "unit tối đa 10 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,10}$", message = "unit không được chứa ký tự điều khiển")
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

    @Size(max = 50, message = "createUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "createUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String createUser;

    @Size(max = 50, message = "updateUser tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "updateUser chỉ gồm chữ, số, '.', '_' hoặc '-'")
    private String updateUser;

    @Size(max = 50, message = "version tối đa 50 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,50}$", message = "version không được chứa ký tự điều khiển")
    private String version;

    @Schema(description = "ID kế toán")
    @Min(value = 1, message = "accountingId phải >= 1")
    @Max(value = 9999999999L, message = "accountingId vượt quá độ dài cột (precision 10)")
    private Long accountingId;

    @Size(min = 1, max = 1, message = "feeType đúng 1 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1}$", message = "feeType không được chứa ký tự điều khiển")
    private String feeType;

    @Schema(description = "ID dịch vụ viễn thông")
    @Min(value = 1, message = "telecomServiceId phải >= 1")
    @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
    private Long telecomServiceId;

    @Size(max = 1000, message = "note tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "note không được chứa ký tự điều khiển")
    private String note;

    @Size(max = 1000, message = "note1 tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "note1 không được chứa ký tự điều khiển")
    private String note1;

    @Size(max = 1000, message = "note2 tối đa 1000 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,1000}$", message = "note2 không được chứa ký tự điều khiển")
    private String note2;

    @Min(value = 1, message = "areaGroupId phải >= 1")
    @Max(value = 9999999999L, message = "areaGroupId vượt quá độ dài cột (precision 10)")
    private Long areaGroupId;

    @Min(value = 1, message = "ownerShopId phải >= 1")
    @Max(value = 9999999999L, message = "ownerShopId vượt quá độ dài cột (precision 10)")
    private Long ownerShopId;

    @Min(value = 1, message = "sapMaterialNumber phải >= 1")
    @Max(value = 9999999999L, message = "sapMaterialNumber vượt quá độ dài cột (precision 10)")
    private Long sapMaterialNumber;

    @Size(max = 6, message = "itTelcol tối đa 6 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,6}$", message = "itTelcol không được chứa ký tự điều khiển")
    private String itTelcol;
}