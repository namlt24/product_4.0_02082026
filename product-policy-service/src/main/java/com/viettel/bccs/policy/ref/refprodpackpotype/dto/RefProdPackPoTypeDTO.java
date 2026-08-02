package com.viettel.bccs.policy.ref.refprodpackpotype.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
    private Long prodPackTypeId;

    @Schema(description = "ID gói sản phẩm", example = "10")
    private Long productPackageId;

    @Schema(description = "ID loại sản phẩm", example = "5")
    private Long productOfferTypeId;

    @Schema(description = "Trạng thái: 0 Không hiệu lực, 1 Hiệu lực", example = "1")
    private String status;

    @Schema(description = "Cập nhật tồn kho: 0 Không, 1 Có", example = "1")
    private String updateStock;

    @Schema(description = "Kiểm tra tồn kho nhân viên: 0 Không, 1 Có", example = "1")
    private String checkStaffStock;

    @Schema(description = "Kiểm tra tồn kho cửa hàng: 0 Không, 1 Có", example = "1")
    private String checkShopStock;

    @Schema(description = "Ngày cập nhật", example = "2024-06-15")
    private Date updateDatetime;

    @Schema(description = "Giới hạn hàng hóa", example = "100")
    private Long limitGoods;

    @Schema(description = "Chuyển IM: 0 Không, 1 Có", example = "1")
    private String transferIm;
}