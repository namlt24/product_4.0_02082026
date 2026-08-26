package com.bccs.gatewaymanager.engine;

import tools.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Trang thai thu thap trong 1 lan thuc thi CompositeOrchestratorEngine cho
 * dung 1 request cua client - path variable, query goc, body goc, va ket qua
 * (da unwrap/allow/deny/rename) cua tung step da chay, theo stepOrder.
 */
public class ExecutionContext {

    private final Map<String, String> pathVariables;
    private final Map<String, String[]> queryParams;
    private final JsonNode requestBody;
    private final Map<Integer, JsonNode> stepResults = new HashMap<>();

    public ExecutionContext(Map<String, String> pathVariables, Map<String, String[]> queryParams, JsonNode requestBody) {
        this.pathVariables = pathVariables == null ? Map.of() : pathVariables;
        this.queryParams = queryParams == null ? Map.of() : queryParams;
        this.requestBody = requestBody;
    }

    public Map<String, String> pathVariables() {
        return pathVariables;
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
