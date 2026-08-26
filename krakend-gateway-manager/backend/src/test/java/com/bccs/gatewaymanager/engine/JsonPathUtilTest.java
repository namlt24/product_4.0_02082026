package com.bccs.gatewaymanager.engine;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonPathUtilTest {

    private final ObjectMapper mapper = new JsonMapper();

    @Test
    void getByDotPath_navigatesNestedObject() {
        JsonNode root = mapper.readTree("{\"shop\":{\"channelTypeId\":1001527}}");
        JsonNode result = JsonPathUtil.getByDotPath(root, "shop.channelTypeId");
        assertThat(result.asLong()).isEqualTo(1001527L);
    }

    @Test
    void getByDotPath_returnsNullWhenSegmentMissing() {
        JsonNode root = mapper.readTree("{\"shop\":{}}");
        assertThat(JsonPathUtil.getByDotPath(root, "shop.channelTypeId")).isNull();
    }

    @Test
    void getByDotPath_returnsNullForBlankOrNullPath() {
        JsonNode root = mapper.readTree("{\"a\":1}");
        assertThat(JsonPathUtil.getByDotPath(root, null)).isNull();
        assertThat(JsonPathUtil.getByDotPath(root, "  ")).isNull();
        assertThat(JsonPathUtil.getByDotPath(null, "a")).isNull();
    }

    @Test
    void aggregateArray_collectsFieldFromEachElement() {
        JsonNode root = mapper.readTree("{\"data\":[{\"code\":\"A\"},{\"code\":\"B\"},{\"code\":\"C\"}]}");
        List<String> result = JsonPathUtil.aggregateArray(root, "data", "code");
        assertThat(result).containsExactly("A", "B", "C");
    }

    @Test
    void aggregateArray_returnsEmptyListWhenArrayMissing() {
        JsonNode root = mapper.readTree("{\"data\":null}");
        assertThat(JsonPathUtil.aggregateArray(root, "notFound", "code")).isEmpty();
    }

    @Test
    void aggregateArray_skipsNullElementValues() {
        JsonNode root = mapper.readTree("{\"data\":[{\"code\":\"A\"},{\"code\":null},{\"code\":\"C\"}]}");
        List<String> result = JsonPathUtil.aggregateArray(root, "data", "code");
        assertThat(result).containsExactly("A", "C");
    }

    @Test
    void setField_addsFieldToObjectNode() {
        ObjectNode target = mapper.createObjectNode();
        JsonPathUtil.setField(target, "prodOfferCodeLst", mapper.createArrayNode().add("X"));
        assertThat(target.has("prodOfferCodeLst")).isTrue();
    }

    @Test
    void toArrayNode_convertsStringListToArrayNode() {
        ArrayNode result = JsonPathUtil.toArrayNode(mapper, List.of("A", "B"));
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).asString()).isEqualTo("A");
    }
}
