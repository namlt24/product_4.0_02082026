package com.bccs.gatewaymanager.exception;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Bien body loi cua Upstream thanh {@link ErrorResponse} cho ma GW-UPSTREAM-4XX/5XX.
 *
 * Body backend BCCS la StandardResponse:
 * <pre>{"code":"BCCS-GLOBAL-VALIDATION-0000","data":null,"message":"Trường x không được để trống",
 *       "messageArgs":[...],"requestId":"...","shortTraceId":"...","timestamp":"...","traceId":"..."}</pre>
 * - Parse duoc: tach code/data/requestId/shortTraceId/traceId ra TOP-LEVEL cua
 *   response loi, message = "Upstream 'x' tra ve HTTP 400: <message nghiep vu>".
 * - Body khong phai JSON (plain text, HTML trang loi...) hoac thieu message:
 *   giu hanh vi cu - gom toan bo raw body vao message, khong co truong phu.
 */
public final class UpstreamErrorBodyParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private UpstreamErrorBodyParser() {
    }

    public static ErrorResponse parse(String errorCode, String upstreamName, int httpStatus, String responseBody) {
        String prefix = "Upstream '" + upstreamName + "' tra ve HTTP " + httpStatus + ": ";
        if (responseBody != null && !responseBody.isBlank()) {
            try {
                JsonNode root = MAPPER.readTree(responseBody);
                if (root != null && root.isObject()) {
                    String beMessage = root.path("message").asText(null);
                    JsonNode dataNode = root.get("data");
                    return ErrorResponse.ofUpstream(
                            errorCode,
                            prefix + (beMessage != null ? beMessage : responseBody),
                            stringOrNull(root, "code"),
                            dataNode == null || dataNode.isNull() || dataNode.isMissingNode() ? null : dataNode,
                            stringOrNull(root, "requestId"),
                            stringOrNull(root, "shortTraceId"),
                            stringOrNull(root, "traceId"));
                }
            } catch (RuntimeException ignored) {
                // khong phai JSON hop le - roi xuong hanh vi cu ben duoi
            }
        }
        return ErrorResponse.of(errorCode, prefix + responseBody);
    }

    private static String stringOrNull(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return v == null || v.isNull() ? null : v.asText();
    }
}
