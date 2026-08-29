package com.bccs.gatewaymanager.engine;

import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

    /**
     * Danh sach stepOrder DA CHAY THANH CONG, theo dung THU TU HOAN THANH THAT
     * (khong phai thu tu khai bao/stepOrder) - dung cho bu tru/rollback nghiep vu
     * (saga best-effort, xem CompositeOrchestratorEngine.runCompensations()): sau
     * khi ca chuoi that bai, duyet danh sach nay theo thu tu NGUOC de "undo" cai
     * gan nhat truoc. Ghi nhan CA tung thanh vien rieng le trong 1 "wave"
     * (parallelGroup) ngay khi RIENG NO thanh cong (doc lap voi cac thanh vien
     * khac trong CUNG wave, kha ca khi ban than wave do cuoi cung that bai vi 1
     * thanh vien khac) - vi duoc append CUNG luc voi putStepResult(), khong phai
     * sau khi ca wave xong. synchronizedList vi co the bi ghi dong thoi tu nhieu
     * thread (giong ly do stepResults dung synchronizedMap o tren).
     */
    private final List<Integer> completedStepOrders = Collections.synchronizedList(new ArrayList<>());

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
        completedStepOrders.add(stepOrder);
    }

    public JsonNode getStepResult(int stepOrder) {
        return stepResults.get(stepOrder);
    }

    /** Ban sao (snapshot) danh sach stepOrder da thanh cong, theo thu tu hoan thanh THAT - xem javadoc field o tren. */
    public List<Integer> completedStepOrders() {
        return new ArrayList<>(completedStepOrders);
    }
}
