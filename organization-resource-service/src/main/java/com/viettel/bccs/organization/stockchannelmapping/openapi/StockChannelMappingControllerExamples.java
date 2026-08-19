package com.viettel.bccs.organization.stockchannelmapping.openapi;

public final class StockChannelMappingControllerExamples {

    private StockChannelMappingControllerExamples() {
    }

    public static final String MAPPING_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000301",
              "requestId": "req-0301",
              "timestamp": "2026-08-19T02:00:00Z",
              "data": [
                {
                  "stockChannelMappingId": 1,
                  "telecomServiceId": 3,
                  "channelTypeId": 2,
                  "stockShopId": 101,
                  "shopId": -1,
                  "staffId": -1,
                  "effectDate": "2026-08-19",
                  "expireDate": null,
                  "status": "1",
                  "createUser": "admin",
                  "createDatetime": "2026-08-19T02:00:00.000+00:00",
                  "updateUser": null,
                  "updateDatetime": null
                }
              ]
            }""";

    public static final String MAPPING_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000302",
              "requestId": "req-0302",
              "timestamp": "2026-08-19T02:00:00Z",
              "data": {
                "stockChannelMappingId": 1,
                "telecomServiceId": 3,
                "channelTypeId": 2,
                "stockShopId": 101,
                "shopId": 12345,
                "staffId": -1,
                "effectDate": "2026-08-19",
                "expireDate": null,
                "status": "1",
                "createUser": "admin",
                "createDatetime": "2026-08-19T02:00:00.000+00:00",
                "updateUser": null,
                "updateDatetime": null
              }
            }""";

}
