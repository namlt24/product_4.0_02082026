package com.viettel.bccs.policy.freecamequipment.openapi;

public final class FreeCamEquipmentControllerExamples {

    private FreeCamEquipmentControllerExamples() {
    }

    public static final String CHECK_REASON_FREE_CAM_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "freeCamEquipmentId": 1,
                  "actionCode": "5001",
                  "reasonId": 9003997310,
                  "areaCode": "H004",
                  "status": "1",
                  "camInsideNumber": 2,
                  "camOutsideNumber": 3,
                  "camMaxNumber": 5,
                  "camInsidePrice": 88888,
                  "camOutsidePrice": 99999,
                  "effectDatetime": "2026-08-01T00:00:00.000+00:00",
                  "expireDatetime": null,
                  "createUser": "system",
                  "updateUser": null,
                  "description": null,
                  "shopCode": null,
                  "staffCode": null,
                  "createDatetime": "2026-08-01T00:00:00.000+00:00",
                  "updateDatetime": null,
                  "customerGroup": null,
                  "customerType": null
                }
              ]
            }""";
}
