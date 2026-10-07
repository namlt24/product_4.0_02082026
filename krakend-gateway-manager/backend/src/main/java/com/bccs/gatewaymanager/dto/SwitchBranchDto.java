package com.bccs.gatewaymanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;

/**
 * 1 nhanh cua step rẽ nhiều nhánh (switch): neu {@code groups} khop (OR giua nhom, AND trong nhom)
 * thi chuyen sang {@code nextStepOrder}. Nhanh DAU TIEN khop duoc chon; khong nhanh nao khop thi
 * dung nextStepOrderIfFalse cua step lam mac dinh. Xem CompositeOrchestratorEngine.evaluateSwitch().
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SwitchBranchDto(List<List<ConditionRuleDto>> groups, Integer nextStepOrder) {

    /** Danh sach nhanh luu CLOB JSON - {"branches":[...]}. */
    public static String toJson(tools.jackson.databind.ObjectMapper objectMapper, List<SwitchBranchDto> branches) {
        if (branches == null || branches.isEmpty()) {
            return null;
        }
        return objectMapper.writeValueAsString(Map.of("branches", branches));
    }

    public static List<SwitchBranchDto> fromJson(tools.jackson.databind.ObjectMapper objectMapper, String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<SwitchBranchDto> branches = objectMapper.readValue(json, Wrapper.class).branches();
            return branches == null ? List.of() : List.copyOf(branches);
        } catch (Exception e) {
            return List.of();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Wrapper(List<SwitchBranchDto> branches) {
    }
}
