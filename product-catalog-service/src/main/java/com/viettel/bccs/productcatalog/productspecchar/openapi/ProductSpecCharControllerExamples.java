package com.viettel.bccs.productcatalog.productspecchar.openapi;

public final class ProductSpecCharControllerExamples {

    private ProductSpecCharControllerExamples() {
    }

    public static final String SPEC_CHAR_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "productSpecCharId": 1,
                "name": "Mau sac",
                "valueType": "1",
                "charType": "2",
                "status": "1",
                "code": "COLOR"
              }
            }""";

    public static final String SPEC_CHAR_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "productSpecCharId": 1,
                  "name": "Mau sac",
                  "valueType": "1",
                  "charType": "2",
                  "status": "1",
                  "code": "COLOR"
                }
              ]
            }""";
}
