package com.bccs.gatewaymanager.engine;

import tools.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Trang thai thu thap trong 1 lan thuc thi CompositeOrchestratorEngine cho
 * dung 1 request cua client - path variable, query goc, body goc, va ket qua
 * (da unwrap/allow/deny/rename) cua tung step da chay, theo stepOrder.
 *
 * stepResults boc qua Collections.synchronizedMap() (khong phai HashMap thuong,
 * cung KHONG dung ConcurrentHashMap) vi khi EndpointConfig.parallelExecution=true,
 * nhieu step doc lap ghi putStepResult() DONG THOI tu nhieu thread khac nhau
 * (xem CompositeOrchestratorEngine.executeStepsInParallel()) - HashMap thuong co
 * the hong cau truc noi bo (mat entry, vong lap vo han khi resize) khi bi ghi
 * dong thoi tu >1 thread. Chon synchronizedMap thay vi ConcurrentHashMap vi
 * ResponseTransformUtil.transform() co the tra ve JsonNode null hop le (response
 * goc null) va duoc luu thang qua putStepResult() - ConcurrentHashMap.put(key, null)
 * nem NullPointerException ngay lap tuc, se lam vo endpoint hien co dang co step
 * tra ve null. Khong anh huong hanh vi duong tuan tu (van chi 1 thread ghi tai 1
 * thoi diem, chi la doi cau truc du lieu ben duoi cho an toan).
 */
public class ExecutionContext {

    private final Map<String, String> pathVariables;
    private final Map<String, String[]> queryParams;
    private final JsonNode requestBody;
    private final Map<Integer, JsonNode> stepResults = Collections.synchronizedMap(new HashMap<>());

    public ExecutionContext(Map<String, String> pathVariables, Map<String, String[]> queryParams, JsonNode requestBody) {
        this.pathVariables = pathVariables == null ? Map.of() : pathVariables;
        this.queryParams = queryParams == null ? Map.of() : queryParams;
        this.requestBody = requestBody;
    }

    public Map<String, String> pathVariables() {
        return pathVariables;
    }

    public Map<String, String[]> queryParams() {
        return queryParams;
    }

    public JsonNode requestBody() {
        return requestBody;
    }

    public void putStepResult(int stepOrder, JsonNode result) {
        stepResults.put(stepOrder, result);
    }

    public JsonNode getStepResult(int stepOrder) {
        return stepResults.get(stepOrder);
    }
}
