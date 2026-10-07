package com.bccs.gatewaymanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 1 RULE CỜ của endpoint - "khop tổ hợp cờ -> hành động" (DDL V5, lưu JSON
 * trong cột endpoint_config.flag_rules). Endpoint có nhiều Rule, đánh giá theo
 * priority TĂNG DẦN, khớp Rule đầu tiên là dừng.
 *
 * NGỮ NGHĨA KHỚP (chốt với user 2026-10-01 - "trong nhóm AND, giữa nhóm OR"):
 * conditions la DANH SACH NHOM - moi nhom la 1 danh sach dieu kien phai DUNG
 * DONG THOI (AND); Rule khop neu CO IT NHAT 1 nhom thoa (OR giua cac nhom):
 *   (c1 ∧ c2) ∨ (c3) — vi du: VIP=true ∧ KYC_DONE=false, HOẶC STAFF=true.
 * UI vẽ: mỗi dieu kien = 1 ô vuông, nhom = khung khoanh vùng các ô; giữa các
 * khung ghi "HOẶC".
 *
 * actionType:
 *  - START_STEP: traffic khớp -> chuỗi step chạy TỪ startStepOrder (thay vì từ
 *    step nhỏ nhất). Step sau vẫn rẽ nhánh theo dữ liệu như hiện tại (P1-5).
 *  - BLOCK: chặn request ngay, trả blockErrorCode/blockMessage, KHÔNG gọi
 *    upstream nào (dùng tắt-bật tính năng / validate trước).
 *
 * Không Rule khớp (hoặc endpoint không khai Rule) -> chạy như cũ từ step nhỏ nhất.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record FlagRuleDto(
        String id,
        String name,
        int priority,
        /**
         * Danh sach NHOM dieu kien (OR giua nhom, AND trong nhom). Moi dieu kien =
         * {flagCode, expected} - flagCode tra cuu GWM_FLAG cua team; expected=true
         * nghia la cờ phải đang BẬT.
         */
        List<List<FlagConditionDto>> conditions,
        /** START_STEP | BLOCK - START_STEP phai co startStepOrder; BLOCK phai co blockErrorCode. */
        String actionType,
        Integer startStepOrder,
        String blockErrorCode,
        String blockMessage
) {

    public FlagRuleDto {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (conditions == null) {
            conditions = List.of();
        }
    }

    /** 1 dieu kien don le trong 1 nhom - cờ flagCode phai co gia tri = expected (boolean). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FlagConditionDto(String flagCode, boolean expected) {
    }

    public static final String ACTION_START_STEP = "START_STEP";
    public static final String ACTION_BLOCK = "BLOCK";

    /** Serialize ra CLOB JSON khi luu endpoint (EndpointMapper) - {"rules":[...]} de tuong lai co metadata. */
    public static String toJson(tools.jackson.databind.ObjectMapper objectMapper, List<FlagRuleDto> rules) {
        if (rules == null || rules.isEmpty()) {
            return null;
        }
        return objectMapper.writeValueAsString(Map.of("rules", rules));
    }

    /** Doc lai tu CLOB JSON khi doc endpoint (EndpointMapper) - null/loi doc = rong (khong co Rule). */
    public static List<FlagRuleDto> fromJson(tools.jackson.databind.ObjectMapper objectMapper, String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<FlagRuleDto> rules = objectMapper.readValue(json, Wrapper.class).rules();
            return rules == null ? List.of() : List.copyOf(rules);
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Shape JSON luu trong CLOB - {"rules":[...]} (de tuong lai co metadata cap endpoint). */
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    record Wrapper(List<FlagRuleDto> rules) {
    }
}
