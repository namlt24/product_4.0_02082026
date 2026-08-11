package com.viettel.bccs.organization.staffext.openapi;

public final class StaffExtControllerExamples {

    private StaffExtControllerExamples() {
    }

    public static final String STAFF_EXT_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000031",
              "requestId": "req-0031",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "staffExtId": 1,
                  "staffId": 12345,
                  "key": "AVATAR_URL",
                  "value": "https://example.com/avatar.jpg",
                  "status": "1",
                  "createUser": "admin",
                  "createDatetime": "2024-01-01T00:00:00.000+00:00",
                  "updateUser": "admin",
                  "updateDatetime": "2024-06-01T00:00:00.000+00:00"
                }
              ]
            }""";

    public static final String STAFF_EXT_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000032",
              "requestId": "req-0032",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "staffExtId": 1,
                "staffId": 12345,
                "key": "AVATAR_URL",
                "value": "https://example.com/avatar.jpg",
                "status": "1",
                "createUser": "admin",
                "createDatetime": "2024-01-01T00:00:00.000+00:00",
                "updateUser": "admin",
                "updateDatetime": "2024-06-01T00:00:00.000+00:00"
              }
            }""";

}
