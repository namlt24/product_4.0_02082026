package com.viettel.bccs.organization.custtype.openapi;

public final class CustTypeControllerExamples {

    private CustTypeControllerExamples() {
    }

    public static final String CUST_TYPE_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000301",
              "requestId": "req-0301",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "custType": "PREPAI",
                "name": "Trả trước",
                "createUser": "admin",
                "createDatetime": "2024-01-01T00:00:00.000+00:00",
                "updateUser": "admin",
                "updateDatetime": "2024-06-01T00:00:00.000+00:00",
                "description": "Khách hàng trả trước",
                "status": "1",
                "groupType": "1",
                "tax": 10,
                "plan": "1",
                "representCust": "0",
                "custTypeId": 1
              }
            }""";

    public static final String CUST_TYPE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000302",
              "requestId": "req-0302",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "custType": "PREPAI",
                  "name": "Trả trước",
                  "createUser": "admin",
                  "createDatetime": "2024-01-01T00:00:00.000+00:00",
                  "updateUser": "admin",
                  "updateDatetime": "2024-06-01T00:00:00.000+00:00",
                  "description": "Khách hàng trả trước",
                  "status": "1",
                  "groupType": "1",
                  "tax": 10,
                  "plan": "1",
                  "representCust": "0",
                  "custTypeId": 1
                }
              ]
            }""";

}
