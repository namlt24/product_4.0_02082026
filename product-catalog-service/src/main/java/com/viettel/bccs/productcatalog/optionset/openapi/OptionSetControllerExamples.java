package com.viettel.bccs.productcatalog.optionset.openapi;

public final class OptionSetControllerExamples {

    private OptionSetControllerExamples() {
    }

    public static final String OPTION_SET_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "optionSetId": 1,
                  "code": "CUST_TYPE_GROUP_TYPE",
                  "name": "Nhóm loại khách hàng",
                  "status": "1",
                  "createUser": "system",
                  "createDatetime": "2026-08-11T00:00:00.000+00:00",
                  "updateUser": null,
                  "updateDatetime": null,
                  "description": "Nhóm loại khách hàng dùng cho MDealer"
                }
              ]
            }""";

    public static final String OPTION_SET_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "optionSetId": 1,
                "code": "CUST_TYPE_GROUP_TYPE",
                "name": "Nhóm loại khách hàng",
                "status": "1",
                "createUser": "system",
                "createDatetime": "2026-08-11T00:00:00.000+00:00",
                "updateUser": null,
                "updateDatetime": null,
                "description": "Nhóm loại khách hàng dùng cho MDealer"
              }
            }""";
}
