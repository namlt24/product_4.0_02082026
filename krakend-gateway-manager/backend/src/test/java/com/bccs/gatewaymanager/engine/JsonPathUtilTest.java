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

    // ---- Index mang qua "[N]" - lay 1 field cua PHAN TU CU THE, hoac ca phan tu do ----

    @Test
    void getByDotPath_layFieldCuaPhanTuMangTheoIndex() {
        JsonNode root = mapper.readTree("{\"data\":[{\"code\":\"A\",\"qty\":1},{\"code\":\"B\",\"qty\":2}]}");
        assertThat(JsonPathUtil.getByDotPath(root, "data[0].code").asString()).isEqualTo("A");
        assertThat(JsonPathUtil.getByDotPath(root, "data[1].qty").asInt()).isEqualTo(2);
    }

    @Test
    void getByDotPath_layNguyenCaPhanTuMangTheoIndex_khongChiRoField() {
        // "data[0]" (khong co ".field" sau) tra ve NGUYEN object cua phan tu do - dung khi
        // muon dung ca 1 phan tu mang lam tham so cho step sau, hoac tra thang lam response
        // qua BackendStep.target.
        JsonNode root = mapper.readTree("{\"data\":[{\"code\":\"A\",\"qty\":1},{\"code\":\"B\",\"qty\":2}]}");
        JsonNode element = JsonPathUtil.getByDotPath(root, "data[0]");
        assertThat(element.isObject()).isTrue();
        assertThat(element.get("code").asString()).isEqualTo("A");
        assertThat(element.get("qty").asInt()).isEqualTo(1);
    }

    @Test
    void getByDotPath_indexVuotQuaSize_traVeNullAnToan() {
        JsonNode root = mapper.readTree("{\"data\":[{\"code\":\"A\"}]}");
        assertThat(JsonPathUtil.getByDotPath(root, "data[5]")).isNull();
        assertThat(JsonPathUtil.getByDotPath(root, "data[5].code")).isNull();
    }

    @Test
    void getByDotPath_indexTrenNodeKhongPhaiMang_traVeNullAnToan() {
        JsonNode root = mapper.readTree("{\"data\":{\"code\":\"A\"}}");
        assertThat(JsonPathUtil.getByDotPath(root, "data[0]")).isNull();
    }

    @Test
    void getByDotPath_nhieuIndexLongNhau_dungChoMangCuaMang() {
        JsonNode root = mapper.readTree("{\"matrix\":[[1,2],[3,4]]}");
        assertThat(JsonPathUtil.getByDotPath(root, "matrix[1][0]").asInt()).isEqualTo(3);
    }

    @Test
    void getByDotPath_dotNotationThuongVanHoatDongDungY_khongDoiHanhVi() {
        // Regression: cu phap dot-notation THUONG (khong co index mang) - 100% mapping da
        // cau hinh truoc khi co tinh nang nay - phai chay dung y het truoc, khong doi 1 chut.
        JsonNode root = mapper.readTree("{\"shop\":{\"channelTypeId\":1001527}}");
        assertThat(JsonPathUtil.getByDotPath(root, "shop.channelTypeId").asLong()).isEqualTo(1001527L);
        JsonNode missing = mapper.readTree("{\"shop\":{}}");
        assertThat(JsonPathUtil.getByDotPath(missing, "shop.channelTypeId")).isNull();
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
