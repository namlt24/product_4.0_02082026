package com.viettel.bccs.policy.reasonpause.openapi;

public final class ReasonPauseControllerExamples {

    private ReasonPauseControllerExamples() {
    }

    public static final String REASON_PAUSE_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "reasonPauseId": 1,
                "numMonth": 3,
                "price": 50000,
                "reasonId": 1,
                "status": "1",
                "createDatetime": "2026-08-11T00:00:00.000+00:00",
                "createUser": "system",
                "updateDatetime": null,
                "updateUser": null
              }
            }""";

    public static final String REASON_PAUSE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "reasonPauseId": 1,
                  "numMonth": 3,
                  "price": 50000,
                  "reasonId": 1,
                  "status": "1",
                  "createDatetime": "2026-08-11T00:00:00.000+00:00",
                  "createUser": "system",
                  "updateDatetime": null,
                  "updateUser": null
                }
              ]
            }""";
}
