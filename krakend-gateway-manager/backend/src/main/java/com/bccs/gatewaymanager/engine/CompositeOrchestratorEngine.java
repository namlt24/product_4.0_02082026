package com.bccs.gatewaymanager.engine;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.MappingTargetType;
import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.service.UpstreamRegistryCache;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Bo may thuc thi composition tai request-time - thay the hoan toan
 * KrakendConfigGenerator (sinh krakend.json tinh) va moi engine ben thu 3
 * (KrakenD/Gravitee). Doc EndpointConfig+Steps+Mappings da duoc
 * EndpointRegistryCache nap san trong bo nho, thuc thi TUNG step theo dung
 * thu tu (hien tai luon TUAN TU trong code - xem ghi chu o handle()), goi
 * upstream qua UpstreamHttpExecutor (co cache/circuit-breaker/retry/bulkhead),
 * roi tra ve ket qua cuoi cung.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CompositeOrchestratorEngine {

    private final UpstreamHttpExecutor upstreamHttpExecutor;
    private final UpstreamRegistryCache upstreamRegistryCache;
    private final ObjectMapper objectMapper;

    /**
     * Thuc thi 1 EndpointConfig cho 1 request that.
     *
     * Non-sequential: moi step DOC LAP, LUON chay het (khong lien quan gi toi
     * re nhanh - xem executeSequentialChain()), roi gop ket qua qua
     * assembleFinalResponse(). Toi uu hoa goi that su song song (vi du qua
     * CompletableFuture cho cac step khong phu thuoc du lieu lan nhau) la
     * huong cai tien co the lam sau, khong chan V1 - hien van la 1 vong lap
     * Java tuan tu, chi khac o cho KHONG doc con tro/dieu kien.
     *
     * Sequential (P1-5): xem executeSequentialChain() - moi step co the tu
     * quyet dinh step TIEP THEO se chay (re nhanh that) thay vi luon la
     * "stepOrder ke tiep co dinh".
     */
    public JsonNode handle(EndpointResponseDto config, Map<String, String> pathVariables,
                            Map<String, String[]> queryParams, String rawRequestBody) {
        JsonNode requestBodyNode = parseBodyOrNull(rawRequestBody);
        ExecutionContext ctx = new ExecutionContext(pathVariables, queryParams, requestBodyNode);

        List<BackendStepDto> orderedSteps = config.steps().stream()
                .sorted((a, b) -> Integer.compare(a.stepOrder(), b.stepOrder()))
                .toList();

        if (!config.sequential()) {
            for (BackendStepDto step : orderedSteps) {
                JsonNode transformed = executeStep(config, step, ctx);
                ctx.putStepResult(step.stepOrder(), transformed);
            }
            return assembleFinalResponse(orderedSteps, ctx);
        }

        return executeSequentialChain(config, orderedSteps, ctx);
    }

    /**
     * Thuc thi chuoi step TUAN TU theo CON TRO (khong phai vong lap for co
     * dinh) - bat dau tu step co stepOrder NHO NHAT, sau moi step tu hoi
     * "step tiep theo la gi" qua determineNextStepOrder(). Step KHONG khai
     * bao dieu kien (conditionOperator=null - dung cho 100% endpoint tu
     * TRUOC P1-5) tai tao DUNG hanh vi cu: van chay het tat ca step theo
     * dung thu tu stepOrder tang dan, khong bo sot/doi thu tu gi ca.
     *
     * Ket qua tra ve = response cua STEP CUOI CUNG THUC SU chay trong lan
     * nay - KHAC voi truoc day (luon la step co stepOrder lon nhat), vi voi
     * re nhanh, chuoi co the ket thuc o BAT KY step nao tuy theo dieu kien.
     */
    private JsonNode executeSequentialChain(EndpointResponseDto config, List<BackendStepDto> orderedSteps, ExecutionContext ctx) {
        Map<Integer, BackendStepDto> stepsByOrder = orderedSteps.stream()
                .collect(Collectors.toMap(BackendStepDto::stepOrder, s -> s));
        List<Integer> allOrders = orderedSteps.stream().map(BackendStepDto::stepOrder).sorted().toList();

        Integer currentOrder = allOrders.isEmpty() ? null : allOrders.get(0);
        // Chong lap vo han: 1 step KHONG DUOC chay lai trong CUNG 1 request - neu
        // nextStepOrderIfTrue/False vo tinh (hoac co chu dich sai) tao thanh vong
        // lap, phai fail-fast tai day thay vi treo request/goi Upstream vo han lan.
        Set<Integer> executedOrders = new HashSet<>();
        JsonNode lastResult = null;

        while (currentOrder != null) {
            if (!executedOrders.add(currentOrder)) {
                throw new BusinessException("GW-BRANCH-LOOP", "Endpoint '" + config.name()
                        + "': phat hien vong lap re nhanh khi thuc thi that (step " + currentOrder
                        + " se bi goi lai) - kiem tra lai cau hinh nextStepOrderIfTrue/nextStepOrderIfFalse.");
            }
            BackendStepDto step = stepsByOrder.get(currentOrder);
            if (step == null) {
                throw new BusinessException("GW-BRANCH-TARGET-404", "Endpoint '" + config.name()
                        + "': dieu kien re nhanh tro toi step " + currentOrder + " khong ton tai.");
            }
            lastResult = executeStep(config, step, ctx);
            ctx.putStepResult(currentOrder, lastResult);
            currentOrder = determineNextStepOrder(step, ctx, allOrders);
        }
        return lastResult == null ? objectMapper.createObjectNode() : lastResult;
    }

    /** Step tiep theo se chay - null = ket thuc chuoi tai day (ket qua step vua chay la response cuoi cung). */
    private Integer determineNextStepOrder(BackendStepDto step, ExecutionContext ctx, List<Integer> allOrders) {
        if (step.conditionOperator() == null) {
            // Khong khai bao dieu kien: next TU NHIEN la stepOrder NHO NHAT con lai
            // LON HON step hien tai (khong cung "+1", khong doi hoi stepOrder lien
            // tuc) - day CHINH LA cach tai tao dung 100% hanh vi cu (chay het moi
            // step theo dung thu tu stepOrder tang dan) cho endpoint khong dung P1-5.
            return allOrders.stream().filter(o -> o > step.stepOrder()).min(Integer::compareTo).orElse(null);
        }
        return evaluateCondition(step, ctx) ? step.nextStepOrderIfTrue() : step.nextStepOrderIfFalse();
    }

    /**
     * So sanh 1 field (STEP_RESPONSE/REQUEST_BODY) voi conditionExpectedValue.
     * Tham chieu toi 1 step CHUA TUNG chay (vi 1 nhanh re khac da di - hoan
     * toan hop le trong 1 do thi co dieu kien) tra ve null AN TOAN qua
     * JsonPathUtil.getByDotPath(null, ...) - KHONG throw, coi nhu "khong ton tai".
     */
    private boolean evaluateCondition(BackendStepDto step, ExecutionContext ctx) {
        JsonNode source;
        if (step.conditionSourceType() == FieldMappingSourceType.REQUEST_BODY) {
            source = ctx.requestBody();
        } else {
            // sourceStepOrder la Integer (nullable) - KHONG duoc goi thang
            // ctx.getStepResult(int) o day vi auto-unbox null se NPE ngay tai
            // call site; step chua khai bao du dieu kien (thieu sourceStepOrder)
            // coi nhu khong co gia tri, khong crash.
            source = step.conditionSourceStepOrder() == null ? null : ctx.getStepResult(step.conditionSourceStepOrder());
        }
        JsonNode value = JsonPathUtil.getByDotPath(source, step.conditionSourceField());
        boolean exists = value != null && !value.isNull();

        return switch (step.conditionOperator()) {
            case EXISTS -> exists;
            case NOT_EXISTS -> !exists;
            case EQUALS -> exists && conditionValueAsString(value).equals(step.conditionExpectedValue());
            case NOT_EQUALS -> !exists || !conditionValueAsString(value).equals(step.conditionExpectedValue());
        };
    }

    private String conditionValueAsString(JsonNode value) {
        return value.isTextual() ? value.asText() : value.toString();
    }

    private JsonNode executeStep(EndpointResponseDto config, BackendStepDto step, ExecutionContext ctx) {
        UpstreamService upstream = upstreamRegistryCache.getById(step.upstreamServiceId());
        if (upstream == null) {
            throw new BusinessException("GW-UPSTREAM-404",
                    "Step '" + step.name() + "' tham chieu Upstream Service khong ton tai (id=" + step.upstreamServiceId() + ").");
        }

        List<FieldMappingDto> mappingsForStep = config.mappings().stream()
                .filter(m -> m.targetStepOrder() == step.stepOrder())
                .toList();

        String resolvedPath = resolvePath(step, ctx, mappingsForStep);
        String resolvedUrl = buildUrl(upstream.getBaseHost(), resolvedPath, mappingsForStep, ctx);
        HttpHeaders headers = buildHeaders(mappingsForStep, ctx);
        JsonNode body = buildBody(step, mappingsForStep, ctx);

        HttpMethod httpMethod = HttpMethod.valueOf(step.method().name());
        JsonNode rawResponse = upstreamHttpExecutor.call(upstream, httpMethod, resolvedUrl, headers, body,
                step.cacheEnabled(), step.cacheTtlSeconds(), step.stepOrder(), step.name(),
                step.connectTimeoutMs(), step.readTimeoutMs());

        JsonNode unwrapped = (step.target() == null || step.target().isBlank())
                ? rawResponse
                : orDefault(JsonPathUtil.getByDotPath(rawResponse, step.target()), objectMapper.createObjectNode());

        return ResponseTransformUtil.transform(unwrapped, step.allowFields(), step.denyFields(), step.fieldRenameMapping());
    }

    /** Thay {token} trong urlPattern: uu tien FieldMapping targetType=PATH, sau do fallback pathVariables cua endpoint (token trung ten). */
    private String resolvePath(BackendStepDto step, ExecutionContext ctx, List<FieldMappingDto> mappingsForStep) {
        String pattern = step.urlPattern();
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\{([a-zA-Z0-9_]+)}").matcher(pattern);
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            String token = matcher.group(1);
            String value = mappingsForStep.stream()
                    .filter(m -> m.targetType() == MappingTargetType.PATH && m.targetParamName().equals(token))
                    .findFirst()
                    .map(m -> resolveMappingValueAsString(m, ctx))
                    .orElseGet(() -> ctx.pathVariables().get(token));
            if (value == null) {
                throw new BusinessException("GW-PATH-TOKEN-MISSING",
                        "Khong tim duoc gia tri cho token '{" + token + "}' o step '" + step.name() + "'.");
            }
            result.append(pattern, lastEnd, matcher.start()).append(java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8));
            lastEnd = matcher.end();
        }
        result.append(pattern.substring(lastEnd));
        return result.toString();
    }

    private String buildUrl(String baseHost, String resolvedPath, List<FieldMappingDto> mappingsForStep, ExecutionContext ctx) {
        // Spring Framework 7 (Boot 4): fromHttpUrl() bi go bo, dung fromUriString() thay the.
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseHost + resolvedPath);
        mappingsForStep.stream()
                .filter(m -> m.targetType() == MappingTargetType.QUERY)
                .forEach(m -> builder.queryParam(m.targetParamName(), resolveMappingValueAsString(m, ctx)));
        return builder.build().toUriString();
    }

    private HttpHeaders buildHeaders(List<FieldMappingDto> mappingsForStep, ExecutionContext ctx) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        mappingsForStep.stream()
                .filter(m -> m.targetType() == MappingTargetType.HEADER)
                .forEach(m -> headers.add(m.targetParamName(), resolveMappingValueAsString(m, ctx)));
        return headers;
    }

    private JsonNode buildBody(BackendStepDto step, List<FieldMappingDto> mappingsForStep, ExecutionContext ctx) {
        List<FieldMappingDto> bodyMappings = mappingsForStep.stream()
                .filter(m -> m.targetType() == MappingTargetType.BODY_FIELD)
                .toList();

        boolean needsBody = step.forwardOriginalBody() || !bodyMappings.isEmpty();
        if (!needsBody) {
            return null;
        }

        // Neu co dung 1 mapping BODY_FIELD voi targetParamName="$body", gia tri do
        // THAY THE TOAN BO body (khong bao trong field nao ca) - dung cho cac API
        // nhan thang 1 mang/gia tri lam body (vi du List<Long> lam @RequestBody,
        // khong phai {"ids": [...]}). Khong ket hop duoc voi forwardOriginalBody
        // hay cac BODY_FIELD khac vi ban chat mau thuan (khong con "object" nao
        // de gop them field vao).
        java.util.Optional<FieldMappingDto> wholeBodyMapping = bodyMappings.stream()
                .filter(m -> "$body".equals(m.targetParamName()))
                .findFirst();
        if (wholeBodyMapping.isPresent()) {
            return resolveMappingValueAsJson(wholeBodyMapping.get(), ctx);
        }

        ObjectNode base = (step.forwardOriginalBody() && ctx.requestBody() != null && ctx.requestBody().isObject())
                ? (ObjectNode) ctx.requestBody().deepCopy()
                : objectMapper.createObjectNode();

        for (FieldMappingDto m : bodyMappings) {
            JsonNode value = resolveMappingValueAsJson(m, ctx);
            JsonPathUtil.setField(base, m.targetParamName(), value);
        }
        return base;
    }

    /** Tra ve gia tri mapping duoi dang JsonNode (dung cho BODY_FIELD - giu nguyen kieu, kem ca mang). */
    private JsonNode resolveMappingValueAsJson(FieldMappingDto m, ExecutionContext ctx) {
        return switch (m.sourceType()) {
            case REQUEST_BODY -> orDefault(JsonPathUtil.getByDotPath(ctx.requestBody(), m.sourceField()), objectMapper.nullNode());
            case QUERY_PARAM -> {
                String value = firstQueryParamValue(ctx, m.sourceField());
                yield value == null ? objectMapper.nullNode() : objectMapper.getNodeFactory().stringNode(value);
            }
            case STEP_RESPONSE -> orDefault(JsonPathUtil.getByDotPath(ctx.getStepResult(m.sourceStepOrder()), m.sourceField()), objectMapper.nullNode());
            case STEP_RESPONSE_ARRAY_AGGREGATE -> JsonPathUtil.toArrayNode(objectMapper,
                    JsonPathUtil.aggregateArray(ctx.getStepResult(m.sourceStepOrder()), m.sourceArrayField(), m.sourceElementField()));
        };
    }

    /** Query param cua CHINH client (khong phai response step nao) - luon la String[] (co the nhieu gia tri trung ten), lay gia tri dau tien. */
    private String firstQueryParamValue(ExecutionContext ctx, String paramName) {
        String[] values = ctx.queryParams().get(paramName);
        return (values == null || values.length == 0) ? null : values[0];
    }

    /** Tra ve gia tri mapping duoi dang String (dung cho PATH/QUERY/HEADER). Mang (ARRAY_AGGREGATE) duoc noi bang dau phay. */
    private String resolveMappingValueAsString(FieldMappingDto m, ExecutionContext ctx) {
        if (m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.STEP_RESPONSE_ARRAY_AGGREGATE) {
            List<String> values = JsonPathUtil.aggregateArray(ctx.getStepResult(m.sourceStepOrder()), m.sourceArrayField(), m.sourceElementField());
            return String.join(",", values);
        }
        if (m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.QUERY_PARAM) {
            return firstQueryParamValue(ctx, m.sourceField());
        }
        JsonNode node = m.sourceType() == com.bccs.gatewaymanager.entity.FieldMappingSourceType.REQUEST_BODY
                ? JsonPathUtil.getByDotPath(ctx.requestBody(), m.sourceField())
                : JsonPathUtil.getByDotPath(ctx.getStepResult(m.sourceStepOrder()), m.sourceField());
        if (node == null || node.isNull()) {
            return null;
        }
        return node.isTextual() ? node.asText() : node.toString();
    }

    /**
     * Chi con duoc goi cho endpoint KHONG sequential (xem handle()) - sequential
     * gio di qua executeSequentialChain() rieng, tra ket qua truc tiep.
     */
    private JsonNode assembleFinalResponse(List<BackendStepDto> orderedSteps, ExecutionContext ctx) {
        if (orderedSteps.size() == 1) {
            int lastStepOrder = orderedSteps.get(orderedSteps.size() - 1).stepOrder();
            return ctx.getStepResult(lastStepOrder);
        }

        // Nhieu step doc lap: gop field cua tat ca step vao 1 object,
        // dung "group" de long rieng neu duoc khai bao (tranh dam field), step khai bao
        // sau se de len field trung ten cua step truoc (giu dung hanh vi cu).
        ObjectNode merged = objectMapper.createObjectNode();
        for (BackendStepDto step : orderedSteps) {
            JsonNode stepResult = ctx.getStepResult(step.stepOrder());
            if (stepResult == null) {
                continue;
            }
            if (step.group() != null && !step.group().isBlank()) {
                merged.set(step.group(), stepResult);
            } else if (stepResult.isObject()) {
                merged.setAll((ObjectNode) stepResult);
            }
        }
        return merged;
    }

    private JsonNode parseBodyOrNull(String rawBody) {
        if (rawBody == null || rawBody.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(rawBody);
        } catch (Exception e) {
            throw new BusinessException("GW-INVALID-BODY", "Body request khong phai JSON hop le: " + e.getMessage());
        }
    }

    private <T> T orDefault(T value, T fallback) {
        return value == null ? fallback : value;
    }
}
