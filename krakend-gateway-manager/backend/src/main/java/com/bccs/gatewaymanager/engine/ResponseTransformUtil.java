package com.bccs.gatewaymanager.engine;

import tools.jackson.databind.JsonNode;
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

    public static JsonNode transform(JsonNode response, List<String> allowFields, List<String> denyFields,
                                       Map<String, String> fieldRenameMapping) {
        if (response == null || !response.isObject()) {
            return response;
        }
        ObjectNode node = (ObjectNode) response.deepCopy();

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
