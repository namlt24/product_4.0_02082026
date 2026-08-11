package com.viettel.bccs.organization.channeltype.openapi;

public final class ChannelTypeControllerExamples {

    private ChannelTypeControllerExamples() {
    }

    public static final String CHANNEL_TYPE_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000101",
              "requestId": "req-0101",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "channelTypeId": 1,
                "name": "Đại lý",
                "status": "1",
                "objectType": "1",
                "isVtUnit": "1",
                "checkComm": "1",
                "stockType": 1,
                "stockReportTemplate": "TEMPLATE_001",
                "totalDebit": 10,
                "allowAddBatch": 1,
                "suffixObjectCode": "VTST",
                "updateStaffOwnerRole": "ROLE_ADMIN",
                "discountPolicyDefaut": "DISC_DEFAULT",
                "pricePolicyDefaut": "PRICE_DEFAULT",
                "updateBlankCodeRole": "ROLE_BLANK",
                "updateObjectInfoRole": "ROLE_OBJ_INFO",
                "updateShopRole": "ROLE_SHOP",
                "code": "CT01",
                "groupChannelTypeId": 5,
                "groupChannelId": 10,
                "isVhrChannel": 0,
                "isCollChannel": 1,
                "isNotBlankCode": 1,
                "createDatetime": "2024-01-01T00:00:00.000+00:00",
                "createUser": "admin",
                "updateUser": "admin",
                "updateDatetime": "2024-06-01T00:00:00.000+00:00",
                "paymentCode": "PAY001",
                "paymentTail": "TAIL",
                "assignCustStatus": 1,
                "description": "Kênh bán hàng Viettel Store"
              }
            }""";

}
