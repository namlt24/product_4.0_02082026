package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.entity.BackendStep;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.entity.FieldMapping;
import com.bccs.gatewaymanager.entity.MappingTargetType;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Bien mot (hoac tat ca) EndpointConfig thanh cau truc JSON chuan cua
 * KrakenD v3 ("version": 3 trong file cau hinh).
 *
 * Co che sequential composite API cua KrakenD:
 * - Endpoint co "sequential": true va nhieu phan tu trong "backend": KrakenD
 *   goi tuan tu tung backend theo thu tu khai bao.
 * - Trong url_pattern cua backend thu N (N >= 1, 0-based), co the tham chieu
 *   field tu response cua backend truoc do bang placeholder Go-template
 *   {{.RespM_field}} voi M la index (0-based) cua backend nguon.
 * - Placeholder {{.RespM_field}} CHI hoat dong trong url_pattern (path + query
 *   string) - day la gioi han cua KrakenD Community Edition, khong ho tro
 *   native cho header. Xem canh bao sinh ra o buildBackendNode().
 */
@Service
@RequiredArgsConstructor
public class KrakendConfigGenerator {

    private final ObjectMapper objectMapper;
    private final EndpointConfigRepository endpointConfigRepository;

    @Value("${gatewaymanager.krakend.name:krakend-gateway}")
    private String gatewayName;

    @Value("${gatewaymanager.krakend.port:8080}")
    private int gatewayPort;

    @Value("${gatewaymanager.krakend.timeout:3000ms}")
    private String defaultTimeout;

    @Value("${gatewaymanager.krakend.cache-ttl:300s}")
    private String defaultCacheTtl;

    /**
     * Sinh toan bo krakend.json tu tat ca endpoint dang luu trong DB - dung khi Deploy.
     * @Transactional bat buoc: EndpointConfig.steps/mappings la FetchType.LAZY va
     * open-in-view=false, neu khong giu session mo trong luc lap qua tung endpoint
     * se gap LazyInitializationException khi goi ep.getSteps()/getMappings().
     */
    @Transactional(readOnly = true)
    public GeneratedConfig generateFullConfig() {
        List<EndpointConfig> all = endpointConfigRepository.findAll();

        ObjectNode root = objectMapper.createObjectNode();
        root.put("version", 3);
        root.put("name", gatewayName);
        root.put("port", gatewayPort);
        root.put("timeout", defaultTimeout);
        root.put("cache_ttl", defaultCacheTtl);
        root.put("output_encoding", "json");

        ObjectNode extraConfig = root.putObject("extra_config");
        extraConfig.putObject("telemetry/logging").put("level", "INFO");

        ArrayNode endpoints = root.putArray("endpoints");
        List<String> warnings = new ArrayList<>();
        for (EndpointConfig ep : all) {
            endpoints.add(buildEndpointNode(ep, warnings));
        }
        return new GeneratedConfig(root, warnings);
    }

    /** Sinh JSON cho DUY NHAT 1 endpoint - dung cho man preview khi dang tao/sua (chua luu DB). */
    public GeneratedConfig previewOne(EndpointConfig ep) {
        List<String> warnings = new ArrayList<>();
        ObjectNode node = buildEndpointNode(ep, warnings);
        return new GeneratedConfig(node, warnings);
    }

    private ObjectNode buildEndpointNode(EndpointConfig ep, List<String> warnings) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("endpoint", ep.getPath());
        node.put("method", ep.getMethod().name());
        node.put("output_encoding", StringUtils.hasText(ep.getOutputEncoding()) ? ep.getOutputEncoding() : "json");

        List<BackendStep> steps = ep.getSteps().stream()
                .sorted(Comparator.comparingInt(BackendStep::getStepOrder))
                .toList();

        if (ep.isSequential() && steps.size() > 1) {
            // QUAN TRONG (xac nhan thuc te tren KrakenD v2.6.3): co che sequential proxy
            // PHAI duoc bat qua "extra_config.proxy.sequential", KHONG PHAI field top-level
            // "sequential" tren endpoint. Neu chi khai "sequential": true o top-level, KrakenD
            // van CHAP NHAN config (khong bao loi) nhung se chay cac backend SONG SONG nhu binh
            // thuong - placeholder {respN_field} bi bien dich thanh Go-template noi bo
            // "{{.RespN_field}}" nhung KHONG BAO GIO duoc render (vi executor tuan tu khong
            // duoc kich hoat), khien URL cuoi cung con nguyen chuoi template chua thay the.
            node.putObject("extra_config").putObject("proxy").put("sequential", true);
        } else if (ep.isSequential() && steps.size() <= 1) {
            warnings.add("Endpoint '" + ep.getPath() + "' danh dau sequential=true nhung chi co "
                    + steps.size() + " backend step - khong can sequential.");
        }

        ArrayNode backends = node.putArray("backend");
        for (BackendStep step : steps) {
            backends.add(buildBackendNode(ep, step, warnings));
        }
        return node;
    }

    private ObjectNode buildBackendNode(EndpointConfig ep, BackendStep step, List<String> warnings) {
        ObjectNode b = objectMapper.createObjectNode();
        String urlPattern = step.getUrlPattern();

        // Cac mapping muc tieu la step nay (nhan du lieu tu 1 step truoc do)
        List<FieldMapping> incoming = ep.getMappings().stream()
                .filter(m -> m.getTargetStepOrder() == step.getStepOrder())
                .toList();

        List<String> queryParts = new ArrayList<>();
        for (FieldMapping m : incoming) {
            if (m.getSourceStepOrder() >= m.getTargetStepOrder()) {
                warnings.add(String.format(
                        "Mapping khong hop le tren step %d: step nguon (%d) phai NHO HON step dich (%d) vi KrakenD chi chay tuan tu tu tren xuong.",
                        step.getStepOrder(), m.getSourceStepOrder(), m.getTargetStepOrder()));
                continue;
            }

            int sourceRespIndex = m.getSourceStepOrder() - 1; // resp0 = step 1, resp1 = step 2 ...
            // Cu phap chinh thuc cua KrakenD (xac nhan tu docs.krakend.io/docs/endpoints/sequential-proxy):
            // "{respN_field}" - N la index 0-based cua backend nguon, field viet thuong.
            // Ho tro dot-notation cho field long nhau (vi du "user.hash" -> {resp0_user.hash}) -
            // GIU NGUYEN dau cham, khong duoc thay bang "_". Khong ho tro truy cap field
            // trong array/collection (chi ho tro object long nhau).
            // (Log debug cua KrakenD co the hien thi dang bien dich noi bo "{{.RespN_field}}" -
            // do la chi tiet trien khai, KHONG phai cu phap can ghi vao url_pattern trong config.)
            String placeholder = "{resp" + sourceRespIndex + "_" + m.getSourceField() + "}";

            switch (m.getTargetType()) {
                case PATH -> {
                    String token = "{" + m.getTargetParamName() + "}";
                    if (!urlPattern.contains(token)) {
                        warnings.add(String.format(
                                "Step %d: khong tim thay path token '%s' trong url_pattern '%s' - kiem tra lai targetParamName.",
                                step.getStepOrder(), token, step.getUrlPattern()));
                    } else {
                        urlPattern = urlPattern.replace(token, placeholder);
                    }
                }
                case QUERY -> queryParts.add(m.getTargetParamName() + "=" + placeholder);
                case HEADER -> warnings.add(String.format(
                        "Step %d: mapping header '%s' (tu resp%d.%s) KHONG duoc KrakenD Community Edition ho tro native chain tu response truoc. "
                                + "Can plugin Martian/Lua (KrakenD Enterprise) hoac middleware rieng. Mapping nay se KHONG duoc ghi vao krakend.json.",
                        step.getStepOrder(), m.getTargetParamName(), sourceRespIndex, m.getSourceField()));
            }
        }

        if (!queryParts.isEmpty()) {
            urlPattern += (urlPattern.contains("?") ? "&" : "?") + String.join("&", queryParts);
        }

        b.put("url_pattern", urlPattern);
        b.put("method", step.getMethod().name());
        b.put("encoding", "json");

        ArrayNode hosts = b.putArray("host");
        step.getHosts().forEach(hosts::add);

        if (StringUtils.hasText(step.getGroup())) {
            b.put("group", step.getGroup());
        }
        if (StringUtils.hasText(step.getTarget())) {
            // "target": KrakenD se "boc vo" - chi giu lai noi dung cua field nay lam response
            // goc cua backend (vi du "data" de bo qua wrapper StandardResponse cua BCCS:
            // {"code":"SUCCESS","data":{...}} -> chi con {...}). mapping/allow/deny/group
            // va cac {respN_field} o step sau deu duoc ap dung TREN noi dung DA duoc boc vo nay.
            b.put("target", step.getTarget());
        }
        if (!step.getAllowFields().isEmpty()) {
            ArrayNode allow = b.putArray("allow");
            step.getAllowFields().forEach(allow::add);
        }
        if (!step.getDenyFields().isEmpty()) {
            ArrayNode deny = b.putArray("deny");
            step.getDenyFields().forEach(deny::add);
        }
        if (!step.getFieldRenameMapping().isEmpty()) {
            ObjectNode mapping = b.putObject("mapping");
            step.getFieldRenameMapping().forEach(mapping::put);
        }
        return b;
    }
}
