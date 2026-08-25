package com.viettel.bccs.productcatalog.product.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Request cho API getListStockTypeWS — migrate từ mono: ExternalServiceForMbccs.getListStockTypeWS
 * (RequestMDealer trong hệ thống cũ). Không đặt @NotBlank/@NotNull cho actionCode/regType/
 * serviceType/productCode: tính "bắt buộc" của actionCode và việc regType/serviceType/productCode
 * rỗng dẫn tới không tìm được dữ liệu được xử lý ở tầng service bằng BusinessException với mã lỗi
 * BCCS-CATALOG-STOCKTYPE cụ thể (giữ đúng hành vi/mã lỗi nghiệp vụ cũ), thay vì lỗi Bean Validation
 * 400 chung chung nếu gắn @NotBlank ở đây.
 */
@Schema(description = "Request lấy danh sách loại hàng hoá (kèm giá) cho 1 gói cước, theo lý do hoà mạng/dịch vụ/mã tác động")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetListStockTypeWSRequest implements Serializable {

    @Schema(description = "Mã tác động (ACTION_CODE) — bắt buộc", example = "00")
    @Size(max = 10, message = "actionCode tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "actionCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String actionCode;

    @Schema(description = "Mã lý do (REASON_CODE)", example = "2")
    @Size(max = 20, message = "regType tối đa 20 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,20}$", message = "regType chỉ gồm chữ, số, '_' hoặc '-'")
    private String regType;

    @Schema(description = "Alias dịch vụ viễn thông (service_alias)", example = "M")
    @Size(max = 3, message = "serviceType tối đa 3 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,3}$", message = "serviceType chỉ gồm chữ và số")
    private String serviceType;

    @Schema(description = "Mã gói cước", example = "POBAS")
    @Size(max = 50, message = "productCode tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String productCode;

    @Schema(description = "Cấu hình hiện thị loại gói trên màn hình chức năng", example = "VIEW_PRODUCT_GROUP_DNTT")
    @Size(max = 50, message = "Cấu hình hiện thị loại gói tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "Cấu hình hiện thị loại gói chỉ gồm chữ, số, '_' hoặc '-'")
    private String viewCode;
}
