package com.bccs.gatewaymanager.engine;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper doc/ghi JSON theo duong dan dot-notation + index mang (vi du
 * "data.id" hoac "data[0].name"), dung chung boi CompositeOrchestratorEngine
 * de trich xuat gia tri tu response step truoc / body goc cua client (ca lam
 * conditionSourceField cho re nhanh, target de "boc vo" response 1 step, VA
 * sourceField cua FieldMapping - CA 3 noi dung chung method nay nen tu dong
 * duoc ho tro index mang, khong can sua rieng tung noi), va gop mang.
 *
 * Ho tro index mang qua ky hieu "[N]" (N la so nguyen khong am) xen giua cac
 * segment dot-notation, vi du "data[0].name" (lay field "name" cua PHAN TU
 * DAU TIEN trong mang "data") hoac chinh "data[0]" (lay NGUYEN phan tu, ca
 * khi no la object/mang long - dung khi muon dung ca 1 phan tu mang lam tham
 * so cho step sau, hoac lam response tra ve qua BackendStep.target). KHONG ho
 * tro wildcard/slice kieu JsonPath day du (vd "$.data[*].code") - truong hop
 * "gop 1 field cua TUNG phan tu mang" van dung
 * FieldMappingSourceType.STEP_RESPONSE_ARRAY_AGGREGATE rieng (xem
 * aggregateArray()), ro rang hon la co gang nhoi wildcard vao day.
 */
public final class JsonPathUtil {

    private JsonPathUtil() {
    }

    /**
     * Tung token cua duong dan: hoac 1 ten field ([a-zA-Z0-9_]+), hoac 1 index
     * mang dang "[so]" - 2 nhanh nay khop LUAN PHIEN theo dung thu tu xuat hien
     * trong chuoi goc, dau "." chi la ky tu phan cach (khong tao token rieng,
     * Matcher.find() tu bo qua).
     */
    private static final Pattern PATH_TOKEN = Pattern.compile("([a-zA-Z0-9_]+)|\\[(\\d+)\\]");

    /**
     * Tra ve JsonNode tai duong dan dot-notation (co the xen index mang qua
     * "[N]"), hoac null neu bat ky buoc nao khong ton tai/sai kieu (segment ten
     * tren 1 node khong phai object, hoac index tren 1 node khong phai mang/vuot
     * qua size) - graceful null xuyen suot, khong throw, dung triet ly da ap
     * dung cho moi cho khac trong engine tham chieu toi du lieu chua chac chan
     * ton tai (vd step chua tung chay trong 1 nhanh re khac).
     */
    public static JsonNode getByDotPath(JsonNode root, String dotPath) {
        if (root == null || dotPath == null || dotPath.isBlank()) {
            return null;
        }
        JsonNode current = root;
        Matcher matcher = PATH_TOKEN.matcher(dotPath);
        while (matcher.find()) {
            if (current == null) {
                return null;
            }
            String fieldName = matcher.group(1);
            if (fieldName != null) {
                if (!current.isObject() || !current.has(fieldName)) {
                    return null;
                }
                current = current.get(fieldName);
            } else {
                int index = Integer.parseInt(matcher.group(2));
                if (!current.isArray() || index >= current.size()) {
                    return null;
                }
                current = current.get(index);
            }
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
