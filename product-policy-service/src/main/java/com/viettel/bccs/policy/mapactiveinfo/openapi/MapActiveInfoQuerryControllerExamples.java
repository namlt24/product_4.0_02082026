package com.viettel.bccs.policy.mapactiveinfo.openapi;

public final class MapActiveInfoQuerryControllerExamples {

    private MapActiveInfoQuerryControllerExamples() {
    }

    public static final String FIND_BY_ID_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000004",
              "requestId": "req-0004",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "id": 1,
                "telServiceId": 73,
                "productCode": "PROD001",
                "productName": "Dịch vụ Mobifone",
                "regReasonId": 1,
                "reasonName": "Lý do đăng ký mới",
                "promCode": "PROMO001",
                "promName": "Khuyến mãi tháng 1",
                "channelTypeId": 1,
                "channelName": "Kênh 1",
                "provinceCode": "HN",
                "districtCode": "HN001"
              }
            }""";

    public static final String CHANEL_TYPE_ID_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000005",
              "requestId": "req-0005",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": 1
            }""";

    public static final String CHANEL_TYPE_ID_REQUEST_EXAMPLE = """
            {
              "shopChanelTypeId": 1,
              "staffId": 12345,
              "shopId": 12345
            }""";

    public static final String REINDEX_ELASTICSEARCH_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000006",
              "requestId": "req-0006",
              "timestamp": "2026-08-15T02:00:00Z",
              "data": 332209
            }""";

    public static final String SEARCH_ELASTICSEARCH_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000007",
              "requestId": "req-0007",
              "timestamp": "2026-08-15T02:00:00Z",
              "data": [
                {
                  "id": 1,
                  "telServiceId": 73,
                  "productCode": "PROD001",
                  "provinceCode": "HN",
                  "staffCode": "NV001",
                  "actionCode": "00",
                  "payType": "1"
                }
              ]
            }""";
}
