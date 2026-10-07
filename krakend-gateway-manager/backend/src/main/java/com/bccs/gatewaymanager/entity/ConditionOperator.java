package com.bccs.gatewaymanager.entity;

/**
 * Toan tu so sanh dung cho dieu kien re nhanh cua 1 BackendStep - xem
 * CompositeOrchestratorEngine.evaluateCondition().
 *
 * 4 toan tu so sanh SO (GREATER_THAN...LESS_THAN_OR_EQUAL): conditionExpectedValue
 * BAT BUOC phai la chuoi parse duoc thanh so (validate luc luu - xem
 * EndpointService.validateBranching()), gia tri lay tu response cung phai la
 * so (JSON number hoac chuoi so) - khac EQUALS/NOT_EQUALS (so sanh CHUOI, chap
 * nhan bat ky gia tri nao).
 */
public enum ConditionOperator {
    EQUALS, NOT_EQUALS, EXISTS, NOT_EXISTS,
    GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL,

    /**
     * Gia tri resolve duoc la 1 MANG - kiem tra co PHAN TU nao khop
     * conditionExpectedValue (so dang text, dung lai conditionValueAsString())
     * hay khong. Vi du field "permissionCodes" la ["A","B"], CONTAINS "A" -> true.
     * Gia tri KHONG phai mang (vd 1 chuoi/so don) -> coi la KHONG khop (fail-safe),
     * KHONG lam chung voi so khop chuoi con (substring) de tranh mo ho ngu nghia -
     * xem CompositeOrchestratorEngine.evaluateSingleRule().
     */
    CONTAINS, NOT_CONTAINS,

    /** conditionExpectedValue la MANG JSON cua cac gia tri, vd ["TT_02","TT_03"] - dung khi gia tri nam trong 1 danh sach co dinh. */
    IN, NOT_IN,

    /** So sanh CHUOI theo tien to/hau to/chua (khac CONTAINS - ap dung cho mang). */
    STARTS_WITH, ENDS_WITH, STRING_CONTAINS,

    /** Chuoi rong, mang rong hoac khong ton tai coi la RONG. Khong can conditionExpectedValue. */
    IS_EMPTY, IS_NOT_EMPTY,

    /** Khop TOAN BO gia tri theo regex (java.util.regex) - co gioi han thoi gian chay, xem CompositeOrchestratorEngine. */
    MATCHES_REGEX,

    /**
     * Kich thuoc gia tri: so phan tu cua MANG, so ky tu cua CHUOI, so key cua OBJECT.
     * Gia tri khong co kich thuoc (so, boolean, null) -> khong khop. expectedValue la SO.
     */
    SIZE_EQUALS, SIZE_GREATER_THAN, SIZE_GREATER_THAN_OR_EQUAL, SIZE_LESS_THAN, SIZE_LESS_THAN_OR_EQUAL;

    /** Toan tu nao can gia tri de so sanh (EXISTS/NOT_EXISTS/IS_EMPTY/IS_NOT_EMPTY thi khong). */
    public boolean needsExpectedValue() {
        return this != EXISTS && this != NOT_EXISTS && this != IS_EMPTY && this != IS_NOT_EMPTY;
    }
}
