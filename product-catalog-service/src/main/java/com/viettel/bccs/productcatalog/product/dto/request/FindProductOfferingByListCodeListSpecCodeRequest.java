package com.viettel.bccs.productcatalog.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;


@Schema(description = "Request tìm sản phẩm (product_offering) theo danh sách mã đặc tính, tuỳ chọn lọc thêm theo danh sách mã sản phẩm")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FindProductOfferingByListCodeListSpecCodeRequest implements Serializable {

    @Schema(description = "Danh sách mã sản phẩm cần lọc (product_offering.code) — không truyền thì lấy tất cả sản phẩm khớp lstSpecCode",
            example = "[\"CODE_001\", \"CODE_002\"]")
    @Size(max = 1000, message = "lstProductOfferCode tối đa 1000 phần tử")
    private List<String> lstProductOfferCode;

    @Schema(description = "Danh sách mã đặc tính cần khớp (product_spec_char.code) — bắt buộc, ít nhất 1 phần tử; " +
            "validate thủ công trong ProductOfferingService (ném BCCS-PRODUCT-VALIDATE-0001 nếu rỗng) thay vì Bean Validation",
            example = "[\"IS_CONNECTED\", \"DATA_CAP\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(max = 1000, message = "lstSpecCode tối đa 1000 phần tử")
    private List<String> lstSpecCode;

    @Schema(description = "ID loại sản phẩm (product_offer_type_id) — mặc định 200 (PRODUCT_CODE) nếu không truyền", example = "200")
    @Size(max = 10, message = "productOfferType tối đa 10 ký tự")
    @Pattern(regexp = "^[0-9]{0,10}$", message = "productOfferType chỉ gồm chữ số")
    private String productOfferType;
}
