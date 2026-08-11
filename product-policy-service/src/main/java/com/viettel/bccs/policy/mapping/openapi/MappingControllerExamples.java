package com.viettel.bccs.policy.mapping.openapi;

public final class MappingControllerExamples {

    private MappingControllerExamples() {
    }

    public static final String SALE_SERVICE_CODE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": ["SS001", "SS002"]
            }""";

    public static final String REASON_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": []
            }""";
}
