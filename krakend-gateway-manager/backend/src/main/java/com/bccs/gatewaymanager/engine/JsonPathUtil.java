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

    /**
     * Tim PHAN TU DAU TIEN trong mang tai arrayPath co gia tri tai filterField (dot-path
     * TINH TU PHAN TU, khong phai goc) khop VAN BAN voi filterValue, roi tra ve gia tri
     * tai extractField (dot-path, cung TINH TU PHAN TU DO). Khong tim thay mang, khong
     * co phan tu khop, hoac extractField khong ton tai -> null (khong throw, cung triet
     * ly graceful null voi getByDotPath()/aggregateArray()). Vi du: mang "data" gom cac
     * object co "productSpecCharDTO.code"/"productSpecCharValueDTO.value" rieng -
     * findInArray(root, "data", "productSpecCharDTO.code", "GET_IP_BY_DOMAIN",
     * "productSpecCharValueDTO.value") lay dung "value" cua PHAN TU co code do - dung cho
     * FieldMappingSourceType.STEP_RESPONSE_ARRAY_FIND.
     */
    public static JsonNode findInArray(JsonNode root, String arrayPath, String filterField, String filterValue, String extractField) {
        JsonNode arrayNode = getByDotPath(root, arrayPath);
        if (arrayNode == null || !arrayNode.isArray()) {
            return null;
        }
        for (JsonNode element : arrayNode) {
            JsonNode filterNode = getByDotPath(element, filterField);
            if (filterNode == null || filterNode.isNull()) {
                continue;
            }
            String text = filterNode.isTextual() ? filterNode.asText() : filterNode.toString();
            if (text.equals(filterValue)) {
                return getByDotPath(element, extractField);
            }
        }
        return null;
    }

    /** Set 1 field vao ObjectNode (dung khi dung body gui di step sau qua targetType=BODY_FIELD). */
    public static void setField(ObjectNode target, String fieldName, JsonNode value) {
        target.set(fieldName, value);
    }

    /**
     * GHI 1 gia tri vao ObjectNode theo duong dan dot-notation (co the xen
     * index mang "[N]") - doi xung voi getByDotPath() o tren, dung cho tinh nang
     * ResponseMapping (ghép field tu step nao do vao response cuoi, targetField
     * co the long nhau vd "data.code").
     *
     * Segment TEN node trung gian THIEU duoc TU TAO (putObject) - neu phai bat
     * buoc khai bao day du cac node trung gian thi cau hinh Mapping kha nhieu
     * dong do du; segment tren node KHONG PHAI object, hoac index mang vuot
     * size/mang thieu thi BO QUA (tra false) - caller log.warn, khong throw
     * (du lieu upstream that khong chac chan dang nao cung ton tai).
     *
     * Tra ve true neu ghi thanh cong, false neu bo qua.
     */
    public static boolean setByDotPath(ObjectNode root, String dotPath, JsonNode value) {
        if (root == null || dotPath == null || dotPath.isBlank() || value == null) {
            return false;
        }
        Matcher matcher = PATH_TOKEN.matcher(dotPath);
        List<Token> tokens = new ArrayList<>();
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                tokens.add(new Token(matcher.group(1), -1));
            } else {
                tokens.add(new Token(null, Integer.parseInt(matcher.group(2))));
            }
        }
        if (tokens.isEmpty()) {
            return false;
        }
        JsonNode current = root;
        for (int t = 0; t < tokens.size(); t++) {
            Token token = tokens.get(t);
            boolean last = (t == tokens.size() - 1);
            if (token.name != null) {
                if (!(current instanceof ObjectNode on)) {
                    return false;
                }
                if (last) {
                    on.set(token.name, value);
                    return true;
                }
                JsonNode next = on.get(token.name);
                if (next == null || next.isNull() || (!next.isObject() && !next.isArray())) {
                    // Node trung gian thieu (hoac bi scalar chan duong) - tao moi
                    // dung KIEU cua token KE TIEP (object cho ten, array cho [N]).
                    Token nextToken = tokens.get(t + 1);
                    current = nextToken.name != null ? on.putObject(token.name) : on.putArray(token.name);
                } else {
                    current = next;
                }
            } else {
                int index = token.index;
                if (last) {
                    if (!(current instanceof ArrayNode an) || index >= current.size()) {
                        return false;
                    }
                    an.set(index, value);
                    return true;
                }
                if (!(current instanceof ArrayNode an) || index >= current.size()) {
                    return false;
                }
                current = an.get(index);
            }
        }
        return false;
    }

    private record Token(String name, int index) {
    }

    /** Chuyen 1 danh sach String thanh ArrayNode de gan vao body (dung cho ket qua aggregateArray). */
    public static ArrayNode toArrayNode(tools.jackson.databind.ObjectMapper mapper, List<String> values) {
        ArrayNode arrayNode = mapper.createArrayNode();
        values.forEach(arrayNode::add);
        return arrayNode;
    }
}
