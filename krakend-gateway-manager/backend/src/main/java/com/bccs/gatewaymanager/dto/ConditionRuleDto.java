package com.bccs.gatewaymanager.dto;

import com.bccs.gatewaymanager.entity.ConditionOperator;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 1 nut trong cay dieu kien cua {@link StepConditionGroupsDto}. Co 3 loai:
 * <ul>
 *   <li>LA (leaf): sourceType + sourceField + operator + (expectedValue hoac vế phải compareSource*).
 *       sourceType=SYSTEM_FLAG thi sourceField la ma co.</li>
 *   <li>NHOM (group): children != null, quantifier = null. logic = AND | OR (mac dinh AND).</li>
 *   <li>LAP MANG (quantifier): children != null, quantifier = ANY | ALL | COUNT.
 *       sourceType/sourceField tro toi MANG can duyet; children la dieu kien ap len TUNG phan tu
 *       (logic = AND | OR giua cac dieu kien con). Ben trong children, REQUEST_BODY doc field
 *       TU PHAN TU dang xet (khong phai body goc). COUNT: so phan tu khop <countOperator> countValue.</li>
 * </ul>
 * compareSource* (tuy chon, chi cho la): so sanh 2 field voi nhau.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ConditionRuleDto(
        FieldMappingSourceType sourceType,
        String sourceField,
        /** Chi dung khi sourceType=STEP_RESPONSE - tham chieu 1 step DA chay truoc do. */
        Integer sourceStepOrder,
        ConditionOperator operator,
        /** Gia tri hang so de so sanh. Bo qua khi co compareSourceField. */
        String expectedValue,
        /** Nut NHOM / LAP MANG: danh sach nut con. null = la LA. */
        List<ConditionRuleDto> children,
        /** Chi co y nghia voi nut NHOM / LAP MANG: "AND" (mac dinh) hoac "OR". */
        String logic,
        /** So sanh 2 field: nguon cua ve phai. null = dung expectedValue. */
        FieldMappingSourceType compareSourceType,
        String compareSourceField,
        Integer compareSourceStepOrder,
        /** Chi voi nut LAP MANG: ANY | ALL | COUNT. null = khong phai nut lap. */
        String quantifier,
        /** Chi voi quantifier=COUNT: toan tu so sanh so phan tu khop (SO sanh so: EQUALS, GT, GTE, LT, LTE). */
        ConditionOperator countOperator,
        /** Chi voi quantifier=COUNT: nguong so (chuoi so). */
        String countValue
) {
    /** Canonical constructor (13 field) la constructor duy nhat Jackson duoc dung de doc JSON. */
    @JsonCreator
    public ConditionRuleDto {
    }

    public static final String QUANTIFIER_ANY = "ANY";
    public static final String QUANTIFIER_ALL = "ALL";
    public static final String QUANTIFIER_COUNT = "COUNT";

    /** Dieu kien LA (V6, 5 field) - giu tuong thich cac cho goi cu. */
    public ConditionRuleDto(FieldMappingSourceType sourceType, String sourceField, Integer sourceStepOrder,
                            ConditionOperator operator, String expectedValue) {
        this(sourceType, sourceField, sourceStepOrder, operator, expectedValue, null, null, null, null, null, null, null, null);
    }

    /** Dang 10 field (truoc khi co quantifier) - giu tuong thich. */
    public ConditionRuleDto(FieldMappingSourceType sourceType, String sourceField, Integer sourceStepOrder,
                            ConditionOperator operator, String expectedValue, List<ConditionRuleDto> children,
                            String logic, FieldMappingSourceType compareSourceType, String compareSourceField,
                            Integer compareSourceStepOrder) {
        this(sourceType, sourceField, sourceStepOrder, operator, expectedValue, children, logic,
                compareSourceType, compareSourceField, compareSourceStepOrder, null, null, null);
    }

    public static ConditionRuleDto group(String logic, List<ConditionRuleDto> children) {
        return new ConditionRuleDto(null, null, null, null, null, children, logic, null, null, null, null, null, null);
    }

    /** Nut LAP MANG: duyet mang (sourceType/sourceField) va ap children len tung phan tu. */
    public static ConditionRuleDto quantifier(FieldMappingSourceType arraySource, String arrayField, Integer arrayStepOrder,
                                              String quantifier, String logic, List<ConditionRuleDto> children,
                                              ConditionOperator countOperator, String countValue) {
        return new ConditionRuleDto(arraySource, arrayField, arrayStepOrder, null, null, children, logic,
                null, null, null, quantifier, countOperator, countValue);
    }

    /** Nut NHOM thuong (AND/OR) - khong phai nut lap mang. */
    @JsonIgnore
    public boolean isGroup() {
        return children != null && quantifier == null;
    }

    /** Khong dat ten isXxx: Jackson se coi day la getter cua field quantifier va bo qua field do. */
    @JsonIgnore
    public boolean hasQuantifier() {
        return children != null && quantifier != null;
    }
}
