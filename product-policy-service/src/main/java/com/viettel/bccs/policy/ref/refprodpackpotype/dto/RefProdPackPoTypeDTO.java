package com.viettel.bccs.policy.ref.refprodpackpotype.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class RefProdPackPoTypeDTO {

    @Schema(description = "ID loại gói sản phẩm", example = "1")
    @Min(value = 0, message = "prodPackTypeId phải >= 0")
    @Max(value = 9999999999L, message = "prodPackTypeId vượt quá độ dài cột (precision 10)")
    private Long prodPackTypeId;

    @Schema(description = "ID gói sản phẩm", example = "10")
    @Min(value = 0, message = "productPackageId phải >= 0")
    @Max(value = 9999999999L, message = "productPackageId vượt quá độ dài cột (precision 10)")
    private Long productPackageId;

    @Schema(description = "ID loại sản phẩm", example = "5")
    @Min(value = 0, message = "productOfferTypeId phải >= 0")
    @Max(value = 9999999999L, message = "productOfferTypeId vượt quá độ dài cột (precision 10)")
    private Long productOfferTypeId;

    @Schema(description = "Trạng thái: 0 Không hiệu lực, 1 Hiệu lực", example = "1")
    @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
    private String status;

    @Schema(description = "Cập nhật tồn kho: 0 Không, 1 Có", example = "1")
    @Size(min = 1, max = 1, message = "updateStock đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "updateStock chỉ nhận giá trị 0 hoặc 1")
    private String updateStock;

    @Schema(description = "Kiểm tra tồn kho nhân viên: 0 Không, 1 Có", example = "1")
    @Size(min = 1, max = 1, message = "checkStaffStock đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "checkStaffStock chỉ nhận giá trị 0 hoặc 1")
    private String checkStaffStock;

    @Schema(description = "Kiểm tra tồn kho cửa hàng: 0 Không, 1 Có", example = "1")
    @Size(min = 1, max = 1, message = "checkShopStock đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "checkShopStock chỉ nhận giá trị 0 hoặc 1")
    private String checkShopStock;

    @Schema(description = "Ngày cập nhật", example = "2024-06-15")
    private Date updateDatetime;

    @Schema(description = "Giới hạn hàng hóa", example = "100")
    @Min(value = 0, message = "limitGoods phải >= 0")
    @Max(value = 9999999999L, message = "limitGoods vượt quá độ dài cột (precision 10)")
    private Long limitGoods;

    @Schema(description = "Chuyển IM: 0 Không, 1 Có", example = "1")
    @Size(min = 1, max = 1, message = "transferIm đúng 1 ký tự")
    @Pattern(regexp = "^[01]$", message = "transferIm chỉ nhận giá trị 0 hoặc 1")
    private String transferIm;
}
