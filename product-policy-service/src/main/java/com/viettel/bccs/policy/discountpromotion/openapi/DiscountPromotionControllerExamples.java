package com.viettel.bccs.policy.discountpromotion.openapi;

public final class DiscountPromotionControllerExamples {

    private DiscountPromotionControllerExamples() {
    }

    public static final String FIND_BY_ID_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "discountPromotionId": 1,
                "telecomServiceId": "100",
                "code": "KM001",
                "name": "Khuyến mãi tháng 8",
                "type": "1",
                "systemType": "1",
                "discountMethod": "1",
                "discountPolicy": "POL001",
                "subType": "1",
                "monthCommitment": 12,
                "pricePlan": "GOI001",
                "monthAmount": 50000,
                "status": "1",
                "description": null,
                "content": null,
                "areaCode": null,
                "effectDatetime": "2026-08-01T00:00:00.000+00:00",
                "expireDatetime": null,
                "createUser": "system",
                "createDatetime": "2026-08-01T00:00:00.000+00:00",
                "updateUser": null,
                "updateDatetime": null,
                "cycle": 1,
                "listType": null,
                "subListId": null,
                "note": null
              }
            }""";

    public static final String PROMOTION_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "discountPromotionId": 1,
                  "telecomServiceId": "100",
                  "code": "KM001",
                  "name": "Khuyến mãi tháng 8",
                  "type": "1",
                  "systemType": "1",
                  "discountMethod": "1",
                  "discountPolicy": "POL001",
                  "subType": "1",
                  "monthCommitment": 12,
                  "pricePlan": "GOI001",
                  "monthAmount": 50000,
                  "status": "1",
                  "description": null,
                  "content": null,
                  "areaCode": null,
                  "effectDatetime": "2026-08-01T00:00:00.000+00:00",
                  "expireDatetime": null,
                  "createUser": "system",
                  "createDatetime": "2026-08-01T00:00:00.000+00:00",
                  "updateUser": null,
                  "updateDatetime": null,
                  "cycle": 1,
                  "listType": null,
                  "subListId": null,
                  "note": null,
                  "regReasonId": null,
                  "reasonName": null,
                  "subGroupCode": null,
                  "productCode": null
                }
              ]
            }""";
}
