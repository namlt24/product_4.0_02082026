package com.viettel.bccs.area.area.openapi;

public final class AreaControllerExamples {

    private AreaControllerExamples() {
    }

    public static final String AREA_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "areaCode": "A076003",
                  "parentCode": "A076",
                  "areaGroup": "01",
                  "province": "A076",
                  "district": "A076003",
                  "precinct": null,
                  "streetBlock": null,
                  "name": "An Giang",
                  "fullName": "Tinh An Giang",
                  "center": "1",
                  "pstnCode": "0296",
                  "provinceCode": "A076",
                  "status": "1",
                  "createUser": "system",
                  "createDatetime": "2026-08-11T00:00:00.000+00:00",
                  "updateUser": null,
                  "updateDatetime": null,
                  "regionId": null,
                  "vtMapCode": null,
                  "square": null,
                  "population": null,
                  "households": null,
                  "areaType": null,
                  "vnCode": null,
                  "vnName": null,
                  "isNew": null
                }
              ]
            }""";

    public static final String AREA_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "areaCode": "A076",
                "parentCode": null,
                "areaGroup": "01",
                "province": "A076",
                "district": null,
                "precinct": null,
                "streetBlock": null,
                "name": "An Giang",
                "fullName": "Tinh An Giang",
                "center": "1",
                "pstnCode": "0296",
                "provinceCode": "A076",
                "status": "1",
                "createUser": "system",
                "createDatetime": "2026-08-11T00:00:00.000+00:00",
                "updateUser": null,
                "updateDatetime": null,
                "regionId": null,
                "vtMapCode": null,
                "square": null,
                "population": null,
                "households": null,
                "areaType": null,
                "vnCode": null,
                "vnName": null,
                "isNew": null
              }
            }""";
}
