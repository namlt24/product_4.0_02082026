package com.viettel.bccs.policy.mapbusinessskipdebt.openapi;

public final class MapBusinessSkipDebtControllerExamples {

    private MapBusinessSkipDebtControllerExamples() {
    }

    public static final String SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "mapId": 1,
                "actionCode": "ACT001",
                "telecomServiceId": 100,
                "productCode": "PROD001",
                "effectDatetime": "2026-08-01T00:00:00.000+00:00",
                "expireDatetime": null,
                "shopId": 10,
                "staffId": 20,
                "businessNo": "BN001",
                "contractNo": "CN001",
                "status": 1,
                "ibmCode": "IBM001",
                "approveUser": "admin",
                "createUser": "admin",
                "updateUser": null,
                "createDatetime": "2026-08-01T00:00:00.000+00:00",
                "updateDatetime": null
              }
            }""";

    public static final String LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "mapId": 1,
                  "actionCode": "ACT001",
                  "telecomServiceId": 100,
                  "productCode": "PROD001",
                  "effectDatetime": "2026-08-01T00:00:00.000+00:00",
                  "expireDatetime": null,
                  "shopId": 10,
                  "staffId": 20,
                  "businessNo": "BN001",
                  "contractNo": "CN001",
                  "status": 1,
                  "ibmCode": "IBM001",
                  "approveUser": "admin",
                  "createUser": "admin",
                  "updateUser": null,
                  "createDatetime": "2026-08-01T00:00:00.000+00:00",
                  "updateDatetime": null
                }
              ]
            }""";

    public static final String SEARCH_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000003",
              "requestId": "req-0003",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "mapId": "1",
                  "actionCode": "ACT001",
                  "telecomServiceId": "100",
                  "productCode": "PROD001",
                  "effectDateTime": "01/08/2026",
                  "expireDateTime": "",
                  "shopId": "10",
                  "staffId": "20",
                  "businessNo": "BN001",
                  "contractNo": "CN001",
                  "status": "1",
                  "ibmCode": "IBM001",
                  "approveUser": "admin",
                  "createUser": "admin",
                  "updateUser": "",
                  "createDatetime": "01/08/2026",
                  "updateDateTime": ""
                }
              ]
            }""";
}
