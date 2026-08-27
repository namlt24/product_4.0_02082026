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
    GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL
}
