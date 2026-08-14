package com.viettel.bccs.policy.mapskipdebtcharges.openapi;

public final class MapSkipDebtChargesControllerExamples {

    private MapSkipDebtChargesControllerExamples() {
    }

    public static final String MAP_SKIP_DEBT_CHARGES_LIST_REQUEST_EXAMPLE = """
            [
              {
                "id": 1,
                "telServiceId": 1,
                "productCode": "POBAS",
                "actionCode": "00",
                "payType": "1",
                "custType": "1",
                "status": "1"
              }
            ]""";

    public static final String MAP_SKIP_DEBT_CHARGES_FULL_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-14T02:00:00Z",
              "data": []
            }""";
}
