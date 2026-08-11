package com.viettel.bccs.policy.discountpromotioncharuse.openapi;

public final class DiscountPromotionCharUseControllerExamples {

    private DiscountPromotionCharUseControllerExamples() {
    }

    public static final String FIND_BY_ID_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "discountPromotionCharUseId": 1,
                "discountPromotionId": 1,
                "productSpecCharValueId": 1,
                "productSpecCharId": 1,
                "createUser": "system",
                "createDatetime": "2026-08-01T00:00:00.000+00:00",
                "updateUser": null,
                "updateDatetime": null,
                "status": "1",
                "specificValue": null,
                "effectDatetime": "2026-08-01T00:00:00.000+00:00",
                "expireDatetime": null,
                "limited": null,
                "note": null
              }
            }""";
}
