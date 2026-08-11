package com.viettel.bccs.productcatalog.optionset.openapi;

public final class OptionSetValueControllerExamples {

    private OptionSetValueControllerExamples() {
    }

    public static final String OPTION_SET_VALUE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "optionSetValueId": 1,
                  "optionSetId": 1,
                  "optionSetCode": "CUST_TYPE_GROUP_TYPE",
                  "name": "Cá nhân",
                  "value": "1",
                  "status": "1",
                  "description": "Khách hàng cá nhân",
                  "createUser": "system",
                  "createDatetime": "2026-08-11T00:00:00.000+00:00",
                  "updateUser": null,
                  "updateDatetime": null,
                  "parentId": null
                }
              ]
            }""";

    public static final String OPTION_SET_VALUE_MAP_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "CUST_TYPE_GROUP_TYPE": [
                  {
                    "optionSetValueId": 1,
                    "optionSetId": 1,
                    "optionSetCode": "CUST_TYPE_GROUP_TYPE",
                    "name": "Cá nhân",
                    "value": "1",
                    "status": "1",
                    "description": "Khách hàng cá nhân",
                    "createUser": "system",
                    "createDatetime": "2026-08-11T00:00:00.000+00:00",
                    "updateUser": null,
                    "updateDatetime": null,
                    "parentId": null
                  }
                ]
              }
            }""";

    public static final String STRING_VALUE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000003",
              "requestId": "req-0003",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": "1"
            }""";

    public static final String MDEALER_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000004",
              "requestId": "req-0004",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "lstOptionSetValue": [
                  {
                    "optionSetValueId": 1,
                    "optionSetId": 1,
                    "optionSetCode": "SUB_OBJECT_OVER_16_INDIVIDUAL",
                    "name": "Chứng minh nhân dân",
                    "value": "1",
                    "status": "1",
                    "description": null,
                    "createUser": "system",
                    "createDatetime": "2026-08-11T00:00:00.000+00:00",
                    "updateUser": null,
                    "updateDatetime": null,
                    "parentId": null
                  }
                ],
                "needGuardianName": false,
                "code": "000"
              }
            }""";
}
