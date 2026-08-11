package com.viettel.bccs.productcatalog.productspeccharvalue.openapi;

public final class ProductSpecCharValueControllerExamples {

    private ProductSpecCharValueControllerExamples() {
    }

    public static final String SPEC_CHAR_VALUE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "productSpecCharValueId": 1,
                  "productSpecCharId": 1,
                  "valueType": "1",
                  "isDefault": 0,
                  "value": "Do",
                  "status": "1",
                  "name": "Mau do"
                }
              ]
            }""";
}
