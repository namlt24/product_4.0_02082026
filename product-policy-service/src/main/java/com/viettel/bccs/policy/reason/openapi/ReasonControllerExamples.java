package com.viettel.bccs.policy.reason.openapi;

public final class ReasonControllerExamples {

    private ReasonControllerExamples() {
    }

    public static final String REASON_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "reasonId": 1,
                "reasonCode": "HTHM01",
                "reasonType": "NEW",
                "name": "Hòa mạng mới",
                "payType": "1",
                "telService": "1,2,3",
                "description": "Hình thức hòa mạng mới cho thuê bao",
                "status": "1",
                "createUser": "system",
                "createDatetime": "2026-08-11T00:00:00.000+00:00",
                "updateUser": null,
                "updateDatetime": null,
                "limitNumberIsdn": 10,
                "limitNumberUser": 10,
                "type": "1",
                "effectDatetime": "2026-01-01T00:00:00.000+00:00",
                "expireDatetime": null,
                "priority": 1,
                "note": null
              }
            }""";

    public static final String CHECK_ATT_REASON_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": true
            }""";

    public static final String REASON_CHARACTER_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000004",
              "requestId": "req-0004",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": ["COLOR", "SIZE"]
            }""";

    public static final String REASON_DTO_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000003",
              "requestId": "req-0003",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "reasonId": 1,
                  "lstCharUse": null,
                  "listReasonPause": null,
                  "reasonCode": "HTHM01",
                  "reasonType": "NEW",
                  "name": "Hòa mạng mới",
                  "payType": "1",
                  "telService": "1,2,3",
                  "description": "Hình thức hòa mạng mới cho thuê bao",
                  "status": "1",
                  "createUser": "system",
                  "createDatetime": "2026-08-11T00:00:00.000+00:00",
                  "updateUser": null,
                  "updateDatetime": null,
                  "limitNumberIsdn": 10,
                  "limitNumberUser": 10,
                  "type": "1",
                  "effectDatetime": "2026-01-01T00:00:00.000+00:00",
                  "expireDatetime": null,
                  "priority": 1,
                  "note": null
                }
              ]
            }""";
}
