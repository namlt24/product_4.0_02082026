package com.viettel.bccs.policy.mapactiveinfo.openapi;

public final class MapActiveInfoProductControllerExamples {

    private MapActiveInfoProductControllerExamples() {
    }

    public static final String PRODUCT_CODE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000003",
              "requestId": "req-0003",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "productOfferingDTOs": [
                  {
                    "code": "PACKAGE_001",
                    "name": "Gói cước data 50GB",
                    "productOfferingId": 12345,
                    "productOfferTypeId": 1,
                    "telecomServiceId": 1,
                    "status": "1"
                  }
                ]
              }
            }""";
}
