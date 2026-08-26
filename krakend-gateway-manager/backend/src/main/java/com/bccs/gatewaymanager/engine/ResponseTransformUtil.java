package com.bccs.gatewaymanager.engine;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

/**
 * Ap dung allow/deny/fieldRenameMapping len response cua 1 step - tai hien
 * dung hanh vi loc field ma truoc day KrakenD tu lam qua "allow"/"deny"/
 * "mapping" trong krakend.json, gio engine tu thuc thi truc tiep.
 */
public final class ResponseTransformUtil {

    private ResponseTransformUtil() {
    }

    /**
     * Ap dung allow/deny/fieldRenameMapping. Ho tro ca response dang OBJECT
     * (loc truc tiep) va dang ARRAY cua object (loc tung phan tu - vi du 1
     * step tra ve mang danh sach shop, deny "internal_debug_info" tren tung
     * shop). Phan tu khong phai object (so/chuoi/null/mang long) giu nguyen.
     * Gia tri khong phai object cung khong phai array tra ve nguyen ban.
     */
    public static JsonNode transform(JsonNode response, List<String> allowFields, List<String> denyFields,
                                       Map<String, String> fieldRenameMapping) {
        if (response == null) {
            return null;
        }
        if (response.isArray()) {
            ArrayNode result = (ArrayNode) response.deepCopy();
            for (int i = 0; i < result.size(); i++) {
                JsonNode element = result.get(i);
                if (element.isObject()) {
                    result.set(i, transformObject((ObjectNode) element, allowFields, denyFields, fieldRenameMapping));
                }
            }
            return result;
        }
        if (!response.isObject()) {
            return response;
        }
        return transformObject((ObjectNode) response.deepCopy(), allowFields, denyFields, fieldRenameMapping);
    }

    private static ObjectNode transformObject(ObjectNode node, List<String> allowFields, List<String> denyFields,
                                                Map<String, String> fieldRenameMapping) {
        if (allowFields != null && !allowFields.isEmpty()) {
            // Jackson 3: fieldNames() doi thanh propertyNames(), tra ve Collection<String>
            // (khong con Iterator) - copy truoc khi remove de tranh ConcurrentModificationException.
            List<String> toRemove = node.propertyNames().stream()
                    .filter(field -> !allowFields.contains(field))
                    .toList();
            toRemove.forEach(node::remove);
        }

        if (denyFields != null && !denyFields.isEmpty()) {
            denyFields.forEach(node::remove);
        }

        if (fieldRenameMapping != null && !fieldRenameMapping.isEmpty()) {
            fieldRenameMapping.forEach((from, to) -> {
                if (node.has(from)) {
                    node.set(to, node.remove(from));
                }
            });
        }

        return node;
    }
}
