package com.viettel.bccs.productcatalog.productpackagefee.openapi;

public final class ProductPackageFeeControllerExamples {

    private ProductPackageFeeControllerExamples() {
    }

    public static final String FEE_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "productPackageFeeId": 1,
                "productPackageId": 1001,
                "code": "FEE01",
                "name": "Phi hoa mang",
                "status": "1",
                "price": 100000,
                "vat": 10000
              }
            }""";

    public static final String FEE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "productPackageFeeId": 1,
                  "productPackageId": 1001,
                  "code": "FEE01",
                  "name": "Phi hoa mang",
                  "status": "1",
                  "price": 100000,
                  "vat": 10000
                }
              ]
            }""";
}
