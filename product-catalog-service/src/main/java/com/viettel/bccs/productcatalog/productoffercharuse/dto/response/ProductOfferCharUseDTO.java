package com.viettel.bccs.productcatalog.productoffercharuse.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Schema
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOfferCharUseDTO {

    @Schema(description = "ID sử dụng đặc tính sản phẩm", example = "12345")
    @JsonProperty(value = "product_offer_char_use_id")
    private Long productOfferCharUseId;

    @Schema(description = "Thứ tự", example = "1")
    private Long orderChar;

    @Schema(description = "Loại", example = "1")
    private String type;

    @Schema(description = "ID sản phẩm", example = "67890")
    @JsonProperty(value = "product_offering_id")
    private Long productOfferingId;

    @Schema(description = "ID giá trị đặc tính sản phẩm", example = "111")
    @JsonProperty(value = "product_spec_char_value_id")
    private Long productSpecCharValueId;

    @Schema(description = "ID đặc tính sản phẩm", example = "222")
    @JsonProperty(value = "product_spec_char_id")
    private Long productSpecCharId;

    @Schema(description = "Người tạo", example = "admin")
    private String createUser;

    @Schema(description = "Ngày tạo")
    private Date createDatetime;

    @Schema(description = "Người cập nhật", example = "admin")
    private String updateUser;

    @Schema(description = "Ngày cập nhật")
    private Date updateDatetime;

    @Schema(description = "Trạng thái", example = "1")
    private String status;

    @Schema(description = "Giá trị đặc biệt", example = "SPEC_VAL")
    @JsonProperty(value = "specific_value")
    private String specificValue;

    @Schema(description = "Ngày hiệu lực")
    private Date effectDatetime;

    @Schema(description = "Ngày hết hạn")
    private Date expireDatetime;

    @Schema(description = "Giới hạn", example = "1")
    private Long limited;

    @Schema(description = "Mô tả", example = "Mô tả sử dụng đặc tính")
    private String description;
}