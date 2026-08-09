package com.viettel.bccs.productcatalog.productpackagefee.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Schema(description = "Phí gói sản phẩm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ProductPackageFeeDTO implements Serializable {

    @Schema(description = "ID phí gói sản phẩm")
    private Long productPackageFeeId;

    @Schema(description = "ID gói sản phẩm")
    private Long productPackageId;

    @Schema(description = "ID chính sách giá")
    private Long pricePolicyId;

    @Schema(description = "ID loại giá")
    private Long priceTypeId;

    private String code;
    private String name;
    private String description;
    private String status;
    private String packageCode;
    private String packageName;
    private Long price;
    private Long vat;
    private String feeSuite;
    private String modeDistribute;

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

    private Date reasonEffectDate;
    private Date reasonExpireDate;
    private String createUser;
    private String updateUser;
    private String effectType;
    private String realStep;
    private String realStepName;
    private String revenueObj;
    private String revenueObjName;
    private Short priority;
    private String cronExpression;
    private String pricePolicy;
    private String priceType;
    private Long distribute;
    private String action;
    private String productPackageCode;
    private String reasonCode;
    private String reasonName;

    @Builder.Default
    private List<ProductPackageFeeDTO> listFeeSuite = new ArrayList<>();

    private Long fileAttachmentId;
    private Long sapMaterialNumber;
    private String sapMaterialName;
    private String productHierarchy;
    private String typeAllocation;
    private String numMonthAllocation;

    @Builder.Default
    private boolean selectedRow = false;
}