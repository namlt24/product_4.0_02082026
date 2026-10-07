package com.bccs.gatewaymanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 1 dòng ghép field vào RESPONSE CUỐI của endpoint (tính năng ResponseMapping) -
 * khác {@link FieldMappingDto} ở chỗ target là response cuối trả client,
 * KHÔNG phải request của step khác. Field nguồn đọc từ {@code ctx.stepResults}
 * của step {@code sourceStepOrder} (giá trị ĐÃ bóc {@code step.target} + ĐÃ
 * transform allow/deny - nhất quán với FieldMapping sourceType=STEP_RESPONSE).
 *
 * targetField hỗ trợ dot-path lồng nhau (vd "data.code"); node trung gian thiếu
 * được tự tạo. Field nguồn thiếu -> skip (log.warn, không chặn request).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResponseFieldMappingDto(
        int sourceStepOrder,
        String sourceField,
        String targetField,
        int mappingOrder
) {
}
