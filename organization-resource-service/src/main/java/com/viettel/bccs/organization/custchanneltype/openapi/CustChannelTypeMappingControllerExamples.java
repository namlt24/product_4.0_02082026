package com.viettel.bccs.organization.custchanneltype.openapi;

public final class CustChannelTypeMappingControllerExamples {

    private CustChannelTypeMappingControllerExamples() {
    }

    public static final String MAPPING_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000201",
              "requestId": "req-0201",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "custChannelTypeMapId": 1,
                  "custType": "PREPAID",
                  "channelTypeId": 1,
                  "status": "1",
                  "createUser": "admin",
                  "createDatetime": "2024-01-01T00:00:00.000+00:00",
                  "updateUser": "admin",
                  "updateDatetime": "2024-06-01T00:00:00.000+00:00"
                }
              ]
            }""";

    public static final String MAPPING_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000202",
              "requestId": "req-0202",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "custChannelTypeMapId": 1,
                "custType": "PREPAID",
                "channelTypeId": 1,
                "status": "1",
                "createUser": "admin",
                "createDatetime": "2024-01-01T00:00:00.000+00:00",
                "updateUser": "admin",
                "updateDatetime": "2024-06-01T00:00:00.000+00:00"
              }
            }""";

}
