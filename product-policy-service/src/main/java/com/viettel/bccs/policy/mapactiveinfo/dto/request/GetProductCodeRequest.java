package com.viettel.bccs.policy.mapactiveinfo.dto.request;

import com.viettel.bccs.policy.common.dto.FilterRequest;
import com.viettel.bccs.policy.utils.RequiredRoleMap;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO riêng cho API getProductCode — chỉ gồm 11 field thực sự được
 * {@link com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoProductService#getProductCode}
 * đọc, thay vì dùng chung {@link RequestMbccs} (72 field, dùng chung cho 3 endpoint khác nhau với
 * 3 tập con field khác nhau — dễ gây nhầm lẫn field nào thực sự liên quan). Bound/pattern của từng
 * field giữ đúng y hệt bản tương ứng trong RequestMbccs, không đổi nghiệp vụ.
 *
 * <p>Không thêm {@code @NotNull} cho các field bắt buộc (staffCode/payType/actionCode/
 * telecomServiceId/roleMap) — validate "bắt buộc" hiện chạy qua
 * {@code MapActiveInfoProductService.validateCommonProductCodeParams(...)}, ném
 * {@code BusinessException} với mã lỗi cụ thể (BCCS-POLICY-MAPACTIVE-0006/0007/...); nếu thêm
 * {@code @NotNull} sẽ đổi sang lỗi Bean Validation chung chung (400 "Request is invalid"), thay
 * đổi hành vi/response đang có.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class GetProductCodeRequest {

    @Schema(description = "Mã nhân viên", example = "VTT1", maxLength = 40)
    @Size(max = 40, message = "staffCode tối đa 40 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String staffCode;

    @Schema(description = "Hình thức thanh toán", example = "1", maxLength = 1)
    @Size(max = 1, message = "payType tối đa 1 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "payType chỉ gồm chữ hoặc số")
    private String payType;

    @Schema(description = "Mã tác động", example = "00", maxLength = 10)
    @Size(max = 10, message = "actionCode tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "actionCode chỉ gồm chữ, số, '_' hoặc '-'")
    private String actionCode;

    @Schema(description = "Loại dịch vụ", example = "1", maxLength = 10)
    @Size(max = 10, message = "telecomServiceId tối đa 10 ký tự")
    @Pattern(regexp = "^[0-9]{0,10}$", message = "telecomServiceId chỉ gồm số")
    private String telecomServiceId;

    @Schema(description = "mode", example = "1", maxLength = 1)
    @Min(value = 0, message = "mode phải >= 0")
    @Max(value = 9, message = "mode phải <= 9")
    private Integer mode;

    @Schema(description = "ID gói cước", example = "400000607", maxLength = 10)
    @Min(value = 1, message = "offerId phải >= 1")
    @Max(value = 9999999999L, message = "offerId vượt quá độ dài cho phép")
    private Long offerId;

    @Schema(description = "Hình thức thay đổi", example = "1", maxLength = 10)
    @Size(max = 10, message = "changeMethod tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "changeMethod chỉ gồm chữ, số, '_' hoặc '-'")
    private String changeMethod;

    @Schema(description = "Công nghệ", example = "4", maxLength = 10)
    @Size(max = 10, message = "technology tối đa 10 ký tự")
    @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{0,10}$", message = "technology không được chứa ký tự điều khiển")
    private String technology;

    @Schema(description = "Loại hạ tầng", example = "1", maxLength = 10)
    @Size(max = 10, message = "infraType tối đa 10 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_-]{0,10}$", message = "infraType chỉ gồm chữ, số, '_' hoặc '-'")
    private String infraType;

    @Schema(description = "Danh sách mã quyền", example = "BCCS2_SALE_SAUBAN_DVCD_TDMK_QUYEN_P")
    private RequiredRoleMap roleMap;

    @Schema(description = "Danh sách thuộc tính")
    @Size(min = 0, max = 200, message = "listProductSpec tối đa 200 phần tử")
    private List<FilterRequest> listProductSpec;
}
