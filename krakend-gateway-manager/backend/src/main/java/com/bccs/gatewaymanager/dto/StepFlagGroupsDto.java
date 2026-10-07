package com.bccs.gatewaymanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dieu kien CỜ HE THONG nhieu nhom cua 1 step (V5b, theo yeu cau "vande":
 * "khi chon Cờ hệ thống... có thể multiple cờ") - luu JSON trong cot
 * backend_step.condition_flag_groups, mirror dung ngu nghia nhom cua Rule
 * (FlagRuleDto): DIEU KIEN trong nhom la AND (phai dung dong thoi), GIUA CAC
 * NHOM la OR - step re nhanh DUNG (nextStepOrderIfTrue) neu CO IT NHAT 1 nhom
 * thoa, SAI (nextStepOrderIfFalse) neu khong nhom nao thoa.
 *
 * Vi du: (VIP=true ∧ KYC_DONE=false) HOẶC STAFF=true.
 * UI: moi dieu kien = 1 o vuong (chon cờ + toggle BẬT/TẮT), nhom = khung
 * khoanh voi nut "+ Thêm điều kiện (AND)", giua cac khung ghi "HOẶC".
 *
 * Ket hop voi conditionSourceType=SYSTEM_FLAG:
 *  - conditionFlagGroups KHAC null/rong -> engine danh gia NHOM, bo qua
 *    conditionSourceField/conditionExpectedValue don le (field cu van giu
 *    nguyen de backward-compat, khong doc).
 *  - conditionFlagGroups null/rong -> engine dung dieu kien 1 co don le cu
 *    (conditionSourceField + conditionExpectedValue) - 100% endpoint da tao
 *    truoc V5b khong doi hanh vi.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StepFlagGroupsDto(
        String id,
        /**
         * Danh sach NHOM dieu kien (OR giua nhom, AND trong nhom). Moi dieu kien
         * = {flagCode, expected} - flagCode tra cuu GWM_FLAG cua team; expected=true
         * nghia la cờ phai dang BẬT. Khac FlagRuleDto o day khong co priority -
         * chi co 1 danh sach nhom duy nhat cho step.
         */
        List<List<FlagRuleDto.FlagConditionDto>> groups
) {

    public StepFlagGroupsDto {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (groups == null) {
            groups = List.of();
        }
    }

    /** Serialize ra CLOB JSON khi luu step (EndpointMapper) - {"groups":[...]}. */
    public static String toJson(tools.jackson.databind.ObjectMapper objectMapper, List<List<FlagRuleDto.FlagConditionDto>> groups) {
        if (groups == null || groups.isEmpty()) {
            return null;
        }
        return objectMapper.writeValueAsString(Map.of("groups", groups));
    }

    /**
     * Doc lai tu CLOB JSON khi doc step (EndpointMapper) - null/loi doc = rong
     * (step khong dung dieu kien nhom, ve lai dieu kien don le neu co).
     */
    public static List<List<FlagRuleDto.FlagConditionDto>> fromJson(tools.jackson.databind.ObjectMapper objectMapper, String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<List<FlagRuleDto.FlagConditionDto>> groups = objectMapper.readValue(json, Wrapper.class).groups();
            return groups == null ? List.of() : List.copyOf(groups);
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Shape JSON luu trong CLOB - {"groups":[[...],[...]]} (de tuong lai co metadata cap step). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Wrapper(List<List<FlagRuleDto.FlagConditionDto>> groups) {
    }
}
