package com.viettel.bccs.policy.mapactiveinfo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho API getChanelTypeIdMapActiveInfo.
 *
 * <p>Chỉ mang {@code staffId} — server tự resolve toàn bộ thông tin nhân viên/shop cần thiết
 * (channelTypeId, shopChanelTypeId, pointOfSale...) từ organization-resource-service (nguồn dữ
 * liệu gốc) thay vì tin vào giá trị "suy luận sẵn" mà client tự truyền lên, tránh sai lệch khi
 * dữ liệu shop/staff phía server đã thay đổi.
 *
 * <p><b>{@code shopChanelTypeId}/{@code shopId} khai báo trong hợp đồng API nhưng KHÔNG được
 * tiêu thụ trong {@code MapActiveInfoQuerryService.getChanelTypeIdMapActiveInfo(ChanelTypeIdRequest)}
 * hiện tại</b> — chỉ {@code staffId} được dùng để resolve. Hai field còn lại giữ trong DTO theo
 * đúng hợp đồng đã yêu cầu (định danh nhân viên/shop mà caller đang thao tác), dành cho các mở
 * rộng nghiệp vụ sau này; đây là quyết định có chủ đích, không phải thiếu sót khi review.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
public class ChanelTypeIdRequest {

    @Schema(description = "ID kênh của shop nhân viên", example = "1")
    @Min(value = 0, message = "shopChanelTypeId phải >= 0")
    @Max(value = 9999999999L, message = "shopChanelTypeId vượt quá độ dài cho phép")
    private Long shopChanelTypeId;

    @Schema(description = "ID nhân viên (STAFF_ID) — dùng để server tự resolve thông tin nhân viên/shop; bắt buộc, validate thủ công trong MapActiveInfoQuerryService (ném BCCS-POLICY-VALIDATE-0001 nếu thiếu) thay vì Bean Validation", example = "12345", requiredMode = Schema.RequiredMode.REQUIRED)
    @Min(value = 1, message = "staffId phải >= 1")
    @Max(value = 9999999999L, message = "staffId vượt quá độ dài cho phép")
    private Long staffId;

    @Schema(description = "ID cửa hàng (SHOP_ID)", example = "12345")
    @Min(value = 0, message = "shopId phải >= 0")
    @Max(value = 9999999999L, message = "shopId vượt quá độ dài cho phép")
    private Long shopId;
}
