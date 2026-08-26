package com.bccs.gatewaymanager.engine;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseTransformUtilTest {

    private final ObjectMapper mapper = new JsonMapper();

    @Test
    void transform_appliesAllowDenyRenameOnObject() {
        JsonNode response = mapper.readTree("{\"id\":1,\"name\":\"A\",\"secret\":\"x\"}");
        JsonNode result = ResponseTransformUtil.transform(response, List.of(), List.of("secret"),
                Map.of("name", "displayName"));
        assertThat(result.has("secret")).isFalse();
        assertThat(result.has("displayName")).isTrue();
        assertThat(result.get("displayName").asString()).isEqualTo("A");
    }

    @Test
    void transform_appliesFilteringPerElementOnArrayOfObjects() {
        // Finding #5 da fix: truoc day transform() bo qua hoan toan neu response la array,
        // khien denyFields khong loc duoc field nhay cam tren tung phan tu.
        JsonNode response = mapper.readTree(
                "[{\"id\":1,\"secret\":\"x\"},{\"id\":2,\"secret\":\"y\"}]");
        JsonNode result = ResponseTransformUtil.transform(response, List.of(), List.of("secret"), Map.of());
        assertThat(result.isArray()).isTrue();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).has("secret")).isFalse();
        assertThat(result.get(1).has("secret")).isFalse();
        assertThat(result.get(0).get("id").asLong()).isEqualTo(1L);
    }

    @Test
    void transform_arrayWithMixedObjectAndScalarElements_leavesScalarsUnchanged() {
        JsonNode response = mapper.readTree("[{\"id\":1,\"secret\":\"x\"},\"PROD_CODE_1\",42]");
        JsonNode result = ResponseTransformUtil.transform(response, List.of(), List.of("secret"), Map.of());
        assertThat(result.get(0).has("secret")).isFalse();
        assertThat(result.get(1).asString()).isEqualTo("PROD_CODE_1");
        assertThat(result.get(2).asLong()).isEqualTo(42L);
    }

    @Test
    void transform_emptyArray_returnsEmptyArray() {
        JsonNode response = mapper.readTree("[]");
        JsonNode result = ResponseTransformUtil.transform(response, List.of(), List.of("secret"), Map.of());
        assertThat(result.isArray()).isTrue();
        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    void transform_nonObjectNonArray_returnsAsIs() {
        JsonNode response = mapper.readTree("\"PROD_CODE_1\"");
        JsonNode result = ResponseTransformUtil.transform(response, List.of(), List.of("x"), Map.of());
        assertThat(result.asString()).isEqualTo("PROD_CODE_1");
    }

    @Test
    void transform_nullResponse_returnsNull() {
        assertThat(ResponseTransformUtil.transform(null, List.of(), List.of(), Map.of())).isNull();
    }

    @Test
    void transform_allowFields_keepsOnlyAllowedFields() {
        JsonNode response = mapper.readTree("{\"id\":1,\"name\":\"A\",\"secret\":\"x\"}");
        JsonNode result = ResponseTransformUtil.transform(response, List.of("id", "name"), List.of(), Map.of());
        assertThat(result.has("secret")).isFalse();
        assertThat(result.has("id")).isTrue();
        assertThat(result.has("name")).isTrue();
    }
}
