package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tu sinh 1 tai lieu OpenAPI 3.0.3 (dang JSON - KHONG dang YAML, xem ly do o
 * duoi) mo ta 1 composite Endpoint - phuc vu nguoi tieu thu API (frontend
 * khac, mobile app...) hien khong co tai lieu nao ca vi Endpoint la cau hinh
 * dong trong DB, khong phai controller tinh @RequestMapping de springdoc quet
 * duoc.
 *
 * "Best-effort", KHONG chinh xac 100% nhu 1 API tinh: response body la cau
 * truc DO composite chain quyet dinh o runtime (phu thuoc response that cua
 * Upstream), khong the biet truoc schema chinh xac tu cau hinh - chi mo ta
 * duoc "type: object" chung. Request thi suy duoc kha chinh xac hon: path
 * param tu token {x} trong path, query param tu cac FieldMapping co
 * sourceType=QUERY_PARAM, cac field REQUEST_BODY duoc mapping toi.
 *
 * Chon JSON thay vi YAML: OpenAPI spec cho phep ca 2 dinh dang tuong duong,
 * JSON dung thang duoc ObjectMapper da co san (khong can them dependency YAML
 * moi, khong co rui ro loi indent/escape thu cong nhu tu viet YAML bang tay).
 */
@Service
@RequiredArgsConstructor
public class OpenApiGeneratorService {

    private static final Pattern PATH_TOKEN = Pattern.compile("\\{([a-zA-Z0-9_]+)}");

    public Map<String, Object> generate(EndpointResponseDto ep) {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("openapi", "3.0.3");
        doc.put("info", info(ep));
        doc.put("paths", Map.of(ep.path(), Map.of(ep.method().name().toLowerCase(), operation(ep))));
        return doc;
    }

    private Map<String, Object> info(EndpointResponseDto ep) {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("title", ep.name());
        info.put("description", (ep.description() == null || ep.description().isBlank())
                ? "Composite API sinh tu Gateway Manager (" + ep.steps().size() + " backend step)."
                : ep.description());
        info.put("version", "1.0.0");
        return info;
    }

    private Map<String, Object> operation(EndpointResponseDto ep) {
        Map<String, Object> op = new LinkedHashMap<>();
        op.put("summary", ep.name());
        op.put("operationId", "endpoint_" + ep.id());
        List<Map<String, Object>> parameters = new ArrayList<>(pathParameters(ep));
        parameters.addAll(queryParameters(ep));
        op.put("parameters", parameters);

        Map<String, Object> requestBodySchema = requestBodySchema(ep);
        if (requestBodySchema != null) {
            op.put("requestBody", Map.of(
                    "required", true,
                    "content", Map.of("application/json", Map.of("schema", requestBodySchema))));
        }
        op.put("responses", responses());
        return op;
    }

    /** Path param suy tu token {x} trong EndpointConfig.path - dung Y HET regex CompositeOrchestratorEngine.resolvePath() dung de khop token that. */
    private List<Map<String, Object>> pathParameters(EndpointResponseDto ep) {
        Matcher m = PATH_TOKEN.matcher(ep.path());
        var params = new ArrayList<Map<String, Object>>();
        while (m.find()) {
            params.add(Map.of(
                    "name", m.group(1),
                    "in", "path",
                    "required", true,
                    "schema", Map.of("type", "string")));
        }
        return params;
    }

    /** Query param suy tu cac FieldMapping co sourceType=QUERY_PARAM (client PHAI gui query param do de mapping hoat dong). */
    private List<Map<String, Object>> queryParameters(EndpointResponseDto ep) {
        return ep.mappings().stream()
                .filter(m -> m.sourceType() == FieldMappingSourceType.QUERY_PARAM)
                .map(FieldMappingDto::sourceField)
                .filter(f -> f != null && !f.isBlank())
                .distinct()
                .<Map<String, Object>>map(name -> Map.of(
                        "name", name,
                        "in", "query",
                        "required", true,
                        "schema", Map.of("type", "string")))
                .toList();
    }

    /**
     * Suy schema body tu cac FieldMapping co sourceType=REQUEST_BODY (client PHAI gui
     * field do trong body de mapping hoat dong) HOAC tu forwardOriginalBody=true cua
     * bat ky step nao (than body tu do, khong biet truoc field cu the). Tra ve null
     * neu khong co dau hieu nao ca (endpoint nay khong doc gi tu body client).
     */
    private Map<String, Object> requestBodySchema(EndpointResponseDto ep) {
        boolean forwardsOriginalBody = ep.steps().stream().anyMatch(s -> s.forwardOriginalBody());
        var bodyFields = ep.mappings().stream()
                .filter(m -> m.sourceType() == FieldMappingSourceType.REQUEST_BODY)
                .map(FieldMappingDto::sourceField)
                .filter(f -> f != null && !f.isBlank())
                .distinct()
                .toList();

        if (!forwardsOriginalBody && bodyFields.isEmpty()) {
            return null;
        }
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        if (!bodyFields.isEmpty()) {
            Map<String, Object> properties = new LinkedHashMap<>();
            // sourceField co the la duong dan long "shop.channelTypeId" - chi lay TEN
            // FIELD DAU TIEN o cap goc lam property (OpenAPI object schema phang, khong
            // the mo ta chinh xac duong dan long qua 1 dong "properties" don gian).
            bodyFields.forEach(f -> properties.put(f.split("\\.")[0], Map.of("type", "string")));
            schema.put("properties", properties);
        }
        if (forwardsOriginalBody) {
            schema.put("description", "Co it nhat 1 Backend Step dung forwardOriginalBody=true - "
                    + "toan bo body client gui se duoc forward nguyen ven, khong gioi han field.");
        }
        return schema;
    }

    private Map<String, Object> responses() {
        Map<String, Object> errorSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "errorCode", Map.of("type", "string"),
                        "message", Map.of("type", "string"),
                        "timestamp", Map.of("type", "string", "format", "date-time")));
        Map<String, Object> errorContent = Map.of("application/json", Map.of("schema", errorSchema));

        Map<String, Object> responses = new LinkedHashMap<>();
        responses.put("200", Map.of(
                "description", "Ket qua composite chain - cau truc phu thuoc response that cua cac Upstream Service da cau hinh.",
                "content", Map.of("application/json", Map.of("schema", Map.of("type", "object")))));
        responses.put("400", Map.of("description", "Loi validate/nghiep vu (path token thieu, du lieu request khong hop le...).", "content", errorContent));
        responses.put("429", Map.of("description", "Vuot rate limit cua gateway (xem header Retry-After).", "content", errorContent));
        responses.put("502", Map.of("description", "Upstream tra ve loi HTTP that su.", "content", errorContent));
        responses.put("503", Map.of("description", "Circuit breaker dang mo hoac bulkhead day - Upstream tam thoi khong nhan them request.", "content", errorContent));
        responses.put("504", Map.of("description", "Upstream khong phan hoi kip thoi (timeout/connection refused).", "content", errorContent));
        return responses;
    }
}
