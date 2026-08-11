package com.viettel.bccs.productcatalog.telecomservice.openapi;

public final class TelecomServiceControllerExamples {

    private TelecomServiceControllerExamples() {
    }

    public static final String TELECOM_SERVICE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "telecomServiceId": 1,
                "name": "Di động",
                "status": "1",
                "description": "Dịch vụ di động",
                "serviceAlias": "MOB",
                "createUser": "system",
                "createDatetime": "2026-01-01T00:00:00",
                "updateUser": null,
                "updateDatetime": null
              }
            }""";
}
