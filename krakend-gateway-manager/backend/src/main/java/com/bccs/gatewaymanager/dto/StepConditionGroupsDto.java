package com.bccs.gatewaymanager.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Dieu kien NHIEU-nguon NHIEU-nhom cua 1 step (V6) - TONG QUAT HOA dung co
 * che AND-trong-nhom/OR-giua-nhom da co san cho SYSTEM_FLAG (xem
 * StepFlagGroupsDto, V5b) sang cho REQUEST_BODY/STEP_RESPONSE: luu JSON
 * trong cot backend_step.condition_groups, moi dieu kien la 1
 * {@link ConditionRuleDto} day du (khong chi flagCode+expected) nen 1 nhom
 * co the tron ca REQUEST_BODY va STEP_RESPONSE.
 *
 * Vi du dung: client gui mang "permissionCodes" - rẽ nhánh ĐÚNG nếu
 * (permissionCodes CONTAINS "A") VA (fieldX EQUALS "true").
 *
 * Doc lap hoan toan voi conditionFlagGroups (SYSTEM_FLAG) - KHONG hop nhat 2
 * co che trong lan nay (xem javadoc ConditionRuleDto). Uu tien danh gia khi
 * khac null/rong - xem CompositeOrchestratorEngine.evaluateCondition():
 * conditionGroups khac rong -> engine danh gia NHOM nay, bo qua HOAN TOAN
 * dieu kien don le (conditionSourceType/conditionSourceField/...) VA nhanh
 * SYSTEM_FLAG/conditionFlagGroups rieng, du step co khai bao gi o do.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record StepConditionGroupsDto(
        String id,
        /** Danh sach NHOM dieu kien (OR giua nhom, AND trong nhom) - mirror StepFlagGroupsDto.groups. */
        List<List<ConditionRuleDto>> groups
) {

    public StepConditionGroupsDto {
        if (id == null || id.isBlank()) {
            id = UUID.randomUUID().toString();
        }
        if (groups == null) {
            groups = List.of();
        }
    }

    /** Serialize ra CLOB JSON khi luu step (EndpointMapper) - {"groups":[...]}. */
    public static String toJson(tools.jackson.databind.ObjectMapper objectMapper, List<List<ConditionRuleDto>> groups) {
        if (groups == null || groups.isEmpty()) {
            return null;
        }
        return objectMapper.writeValueAsString(Map.of("groups", groups));
    }

    /**
     * Doc lai tu CLOB JSON khi doc step (EndpointMapper) - null/loi doc = rong
     * (step khong dung dieu kien nhom, ve lai dieu kien don le neu co).
     */
    public static List<List<ConditionRuleDto>> fromJson(tools.jackson.databind.ObjectMapper objectMapper, String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            List<List<ConditionRuleDto>> groups = objectMapper.readValue(json, Wrapper.class).groups();
            return groups == null ? List.of() : List.copyOf(groups);
        } catch (Exception e) {
            return List.of();
        }
    }

    /** Shape JSON luu trong CLOB - {"groups":[[...],[...]]} (de tuong lai co metadata cap step). */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Wrapper(List<List<ConditionRuleDto>> groups) {
    }
}
