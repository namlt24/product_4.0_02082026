package com.viettel.bccs.policy.mapactiveinfo.openapi;

public final class MapActiveInfoValidateControllerExamples {

    private MapActiveInfoValidateControllerExamples() {
    }

    public static final String VALIDATE_FOLLOW_MAP_ACTIVE_INFO_NEW_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000010",
              "requestId": "req-0010",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "actionCode": "00",
                "actionName": "Kích hoạt",
                "telServiceId": 73,
                "productCode": "VCONNECT_VBN0",
                "productName": "Dịch vụ Mobifone",
                "regReasonId": 9003998100,
                "reasonName": "Lý do đăng ký mới",
                "promCode": "SBN",
                "channelTypeId": 1,
                "provinceCode": "HCM",
                "customerGroup": "1",
                "customerType": "CQU",
                "payType": "1",
                "status": "1"
              }
            }""";
}
