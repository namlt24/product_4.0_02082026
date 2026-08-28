package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
import com.bccs.gatewaymanager.entity.FieldMappingSourceType;
import com.bccs.gatewaymanager.entity.GatewayMethod;
import com.bccs.gatewaymanager.entity.MappingTargetType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiGeneratorServiceTest {

    private final OpenApiGeneratorService service = new OpenApiGeneratorService();

    private BackendStepDto step(boolean forwardOriginalBody) {
        return new BackendStepDto(null, 1, "step1", GatewayMethod.GET, "/x", "up-1", "up",
                forwardOriginalBody, false, 300, null, null, List.of(), List.of(), Map.of(), null, null,
                null, null,
                null, null, null, null, null, null, null);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> operationOf(Map<String, Object> doc, String path, String method) {
        Map<String, Object> paths = (Map<String, Object>) doc.get("paths");
        Map<String, Object> pathItem = (Map<String, Object>) paths.get(path);
        return (Map<String, Object>) pathItem.get(method);
    }

    @Test
    void sinhDungPathParam_tuTokenTrongPath() {
        EndpointResponseDto ep = new EndpointResponseDto("ep-1", "n", null, "/v1/orders/{orderId}", GatewayMethod.GET,
                true, "json", List.of(step(false)), List.of(), null, null, false, 86400);

        Map<String, Object> doc = service.generate(ep);

        assertThat(doc.get("openapi")).isEqualTo("3.0.3");
        Map<String, Object> op = operationOf(doc, "/v1/orders/{orderId}", "get");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> params = (List<Map<String, Object>>) op.get("parameters");
        assertThat(params).hasSize(1);
        assertThat(params.get(0).get("name")).isEqualTo("orderId");
        assertThat(params.get(0).get("in")).isEqualTo("path");
        assertThat(op).doesNotContainKey("requestBody");
    }

    @Test
    void coMappingREQUEST_BODY_sinhRequestBodyKemDungPropertyGoc() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.REQUEST_BODY, null,
                "shop.channelTypeId", null, null, null, 1, MappingTargetType.QUERY, "userId", 0);
        EndpointResponseDto ep = new EndpointResponseDto("ep-1", "n", null, "/v1/x", GatewayMethod.POST,
                true, "json", List.of(step(false)), List.of(mapping), null, null, false, 86400);

        Map<String, Object> doc = service.generate(ep);
        Map<String, Object> op = operationOf(doc, "/v1/x", "post");

        assertThat(op).containsKey("requestBody");
        @SuppressWarnings("unchecked")
        Map<String, Object> requestBody = (Map<String, Object>) op.get("requestBody");
        @SuppressWarnings("unchecked")
        Map<String, Object> content = (Map<String, Object>) requestBody.get("content");
        @SuppressWarnings("unchecked")
        Map<String, Object> appJson = (Map<String, Object>) content.get("application/json");
        @SuppressWarnings("unchecked")
        Map<String, Object> schema = (Map<String, Object>) appJson.get("schema");
        @SuppressWarnings("unchecked")
        Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
        // "shop.channelTypeId" -> chi lay ten field GOC "shop" (khong the mo ta duong dan long trong 1 object schema phang)
        assertThat(properties).containsKey("shop");
    }

    @Test
    void forwardOriginalBody_sinhRequestBodyDuKhongCoMapping() {
        EndpointResponseDto ep = new EndpointResponseDto("ep-1", "n", null, "/v1/x", GatewayMethod.POST,
                true, "json", List.of(step(true)), List.of(), null, null, false, 86400);

        Map<String, Object> doc = service.generate(ep);
        Map<String, Object> op = operationOf(doc, "/v1/x", "post");

        assertThat(op).containsKey("requestBody");
    }

    @Test
    void khongCoBodyMapping_khongCoForward_khongSinhRequestBody() {
        EndpointResponseDto ep = new EndpointResponseDto("ep-1", "n", null, "/v1/x", GatewayMethod.GET,
                true, "json", List.of(step(false)), List.of(), null, null, false, 86400);

        Map<String, Object> doc = service.generate(ep);
        Map<String, Object> op = operationOf(doc, "/v1/x", "get");

        assertThat(op).doesNotContainKey("requestBody");
    }

    @Test
    void coMappingQUERY_PARAM_sinhDungQueryParameterBatBuoc() {
        FieldMappingDto mapping = new FieldMappingDto(null, FieldMappingSourceType.QUERY_PARAM, null,
                "staffCode", null, null, null, 1, MappingTargetType.QUERY, "staffCode", 0);
        EndpointResponseDto ep = new EndpointResponseDto("ep-1", "n", null, "/v1/x", GatewayMethod.GET,
                true, "json", List.of(step(false)), List.of(mapping), null, null, false, 86400);

        Map<String, Object> doc = service.generate(ep);
        Map<String, Object> op = operationOf(doc, "/v1/x", "get");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> params = (List<Map<String, Object>>) op.get("parameters");
        assertThat(params).hasSize(1);
        assertThat(params.get(0).get("name")).isEqualTo("staffCode");
        assertThat(params.get(0).get("in")).isEqualTo("query");
        assertThat(params.get(0).get("required")).isEqualTo(true);
        // QUERY_PARAM khong dua vao "requestBody" (khac REQUEST_BODY) - day la param tren URL.
        assertThat(op).doesNotContainKey("requestBody");
    }

    @Test
    void luonCoResponses200Va4xx5xxChuan() {
        EndpointResponseDto ep = new EndpointResponseDto("ep-1", "n", null, "/v1/x", GatewayMethod.GET,
                true, "json", List.of(step(false)), List.of(), null, null, false, 86400);

        Map<String, Object> doc = service.generate(ep);
        Map<String, Object> op = operationOf(doc, "/v1/x", "get");
        @SuppressWarnings("unchecked")
        Map<String, Object> responses = (Map<String, Object>) op.get("responses");

        assertThat(responses).containsKeys("200", "400", "429", "502", "503", "504");
    }
}
