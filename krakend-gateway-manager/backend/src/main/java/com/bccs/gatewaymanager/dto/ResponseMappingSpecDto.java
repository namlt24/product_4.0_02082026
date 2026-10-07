package com.bccs.gatewaymanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * Cấu hình "ghép field từ bất kỳ step nào vào response cuối" (tính năng
 * ResponseMapping) của 1 endpoint - lưu JSON String trong cột
 * {@code endpoint_config.response_mappings} (mirror pattern FlagRuleDto),
 * áp ở CUỐI CompositeOrchestratorEngine.handle() cho CẢ 2 chế độ
 * sequential/non-sequential.
 *
 * mode:
 *  - MERGE (mặc định): giữ nguyên kết quả tổng hợp của engine, chỉ BỔ SUNG
 *    các field khai báo (field trùng tên bị giá trị mapping sau ghi đè).
 *  - REPLACE: BỎ kết quả tổng hợp - response cuối ĐÚNG VÀ CHỈ gồm các field
 *    khai báo (giải case giữ envelope BCCS + lọc data: step target=null giữ
 *    raw, khai mapping code/message/... + data.xxx).
 *
 * Rỗng (null/0 mapping) = không dùng - 100% endpoint cũ giữ hành vi cũ.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResponseMappingSpecDto(
        String mode,
        List<ResponseFieldMappingDto> mappings
) {

    public static final String MODE_MERGE = "MERGE";
    public static final String MODE_REPLACE = "REPLACE";

    public ResponseMappingSpecDto {
        if (mode == null || mode.isBlank()) {
            mode = MODE_MERGE;
        }
        if (mappings == null) {
            mappings = List.of();
        }
    }

    /** true khi spec KHÔNG cấu hình gì (null hoặc danh sách mapping rỗng) - engine bỏ qua. */
    public static boolean isEmpty(ResponseMappingSpecDto spec) {
        return spec == null || spec.mappings() == null || spec.mappings().isEmpty();
    }

    /** Serialize ra CLOB JSON khi lưu endpoint (EndpointMapper) - null khi rỗng (không dùng). */
    public static String toJson(tools.jackson.databind.ObjectMapper objectMapper, ResponseMappingSpecDto spec) {
        if (isEmpty(spec)) {
            return null;
        }
        return objectMapper.writeValueAsString(Map.of("mode", spec.mode(), "mappings", spec.mappings()));
    }

    /** Đọc lại từ CLOB JSON khi đọc endpoint (EndpointMapper) - null/blank/lỗi đọc = không dùng. */
    public static ResponseMappingSpecDto fromJson(tools.jackson.databind.ObjectMapper objectMapper, String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, ResponseMappingSpecDto.class);
        } catch (Exception e) {
            return null;
        }
    }
}
