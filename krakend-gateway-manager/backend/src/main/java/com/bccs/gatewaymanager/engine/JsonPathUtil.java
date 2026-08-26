package com.bccs.gatewaymanager.engine;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper doc/ghi JSON theo duong dan dot-notation don gian (vi du "data.id"),
 * dung chung boi CompositeOrchestratorEngine de trich xuat gia tri tu response
 * step truoc / body goc cua client, va gop mang.
 *
 * Chi ho tro dot-notation cho OBJECT long nhau - khong ho tro wildcard/index
 * mang kieu JsonPath day du (vd "$.data[*].code") vi engine da co co che rieng,
 * ro rang hon cho dung truong hop "gop 1 field cua tung phan tu mang" qua
 * FieldMappingSourceType.STEP_RESPONSE_ARRAY_AGGREGATE (xem aggregateArray()).
 */
public final class JsonPathUtil {

    private JsonPathUtil() {
    }

    /** Tra ve JsonNode tai duong dan dot-notation, hoac null neu khong ton tai/khong phai object long nhau. */
    public static JsonNode getByDotPath(JsonNode root, String dotPath) {
        if (root == null || dotPath == null || dotPath.isBlank()) {
            return null;
        }
        JsonNode current = root;
        for (String segment : dotPath.split("\\.")) {
            if (current == null || !current.has(segment)) {
                return null;
            }
            current = current.get(segment);
        }
        return current;
    }

    /**
     * Gop 1 field cua TUNG phan tu trong mang tai arrayPath thanh 1 danh sach.
     * Vi du: arrayPath="data", elementField="code" tren
     * {"data":[{"code":"A"},{"code":"B"}]} -> ["A","B"].
     * Tra ve danh sach rong (khong throw) neu khong tim thay mang - de engine
     * co the tiep tuc voi mang rong thay vi loi cung.
     */
    public static List<String> aggregateArray(JsonNode root, String arrayPath, String elementField) {
        List<String> result = new ArrayList<>();
        JsonNode arrayNode = getByDotPath(root, arrayPath);
        if (arrayNode == null || !arrayNode.isArray()) {
            return result;
        }
        for (JsonNode element : arrayNode) {
            JsonNode fieldValue = element.get(elementField);
            if (fieldValue != null && !fieldValue.isNull()) {
                result.add(fieldValue.isTextual() ? fieldValue.asText() : fieldValue.toString());
            }
        }
        return result;
    }

    /** Set 1 field vao ObjectNode (dung khi dung body gui di step sau qua targetType=BODY_FIELD). */
    public static void setField(ObjectNode target, String fieldName, JsonNode value) {
        target.set(fieldName, value);
    }

    /** Chuyen 1 danh sach String thanh ArrayNode de gan vao body (dung cho ket qua aggregateArray). */
    public static ArrayNode toArrayNode(tools.jackson.databind.ObjectMapper mapper, List<String> values) {
        ArrayNode arrayNode = mapper.createArrayNode();
        values.forEach(arrayNode::add);
        return arrayNode;
    }
}
