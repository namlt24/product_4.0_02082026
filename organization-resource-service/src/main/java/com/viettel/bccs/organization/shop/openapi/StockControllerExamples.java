package com.viettel.bccs.organization.shop.openapi;

public final class StockControllerExamples {

    private StockControllerExamples() {
    }

    public static final String STOCK_MBCCS_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000045",
              "requestId": "req-0045",
              "timestamp": "2026-08-19T02:00:00Z",
              "data": [
                {
                  "stockId": 12345,
                  "code": "NV_001",
                  "name": "Nguyễn Văn A",
                  "type": "2"
                },
                {
                  "stockId": 10000,
                  "code": "VTST_HN_001",
                  "name": "Viettel Store Hà Nội",
                  "type": "1"
                },
                {
                  "stockId": 888,
                  "code": "VTST_HN_KS_001",
                  "name": "Kho số chức năng Hà Nội",
                  "type": "3"
                }
              ]
            }""";

    public static final String VALIDATE_STOCK_MAPPING_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000046",
              "requestId": "req-0046",
              "timestamp": "2026-08-20T02:00:00Z",
              "data": true
            }""";

}
