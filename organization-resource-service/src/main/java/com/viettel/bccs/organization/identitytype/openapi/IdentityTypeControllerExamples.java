package com.viettel.bccs.organization.identitytype.openapi;

public final class IdentityTypeControllerExamples {

    private IdentityTypeControllerExamples() {
    }

    public static final String IDENTITY_TYPE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000401",
              "requestId": "req-0401",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "idType": "IDC",
                  "name": "Chứng minh nhân dân",
                  "status": "1",
                  "description": "Giấy tờ tùy thân CMND/CCCD",
                  "minLength": 9,
                  "maxLength": 12,
                  "valuePattern": "^[0-9]{9,12}$"
                }
              ]
            }""";

    public static final String IDENTITY_TYPE_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000402",
              "requestId": "req-0402",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "idType": "IDC",
                "name": "Chứng minh nhân dân",
                "status": "1",
                "description": "Giấy tờ tùy thân CMND/CCCD",
                "minLength": 9,
                "maxLength": 12,
                "valuePattern": "^[0-9]{9,12}$"
              }
            }""";

}
