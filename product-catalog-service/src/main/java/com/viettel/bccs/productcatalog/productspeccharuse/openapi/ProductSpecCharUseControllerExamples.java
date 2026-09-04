package com.viettel.bccs.productcatalog.productspeccharuse.openapi;

public final class ProductSpecCharUseControllerExamples {

    private ProductSpecCharUseControllerExamples() {
    }

    public static final String SPEC_CHAR_USE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "prodSpecCharUseId": 1,
                  "orderChar": 1,
                  "productSpecId": 1,
                  "productSpecCharId": 1,
                  "productSpecCharValueId": 1,
                  "status": "1",
                  "systemType": "VAS",
                  "isRequired": "0"
                }
              ]
            }""";
}
