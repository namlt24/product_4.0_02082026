package com.viettel.bccs.productcatalog.productoffercharuse.openapi;

public final class ProductOfferCharUseControllerExamples {

    private ProductOfferCharUseControllerExamples() {
    }

    public static final String SPEC_CHAR_MAP_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "500001": [
                  {
                    "productSpecCharId": 12345,
                    "name": "Giá cước tháng",
                    "code": "MONTHLY_FEE",
                    "charType": "PRICE_PLAN",
                    "status": "1",
                    "productOfferingId": 500001
                  }
                ]
              }
            }""";

    public static final String ATTRIBUTE_VALUE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": "50000"
            }""";
}
