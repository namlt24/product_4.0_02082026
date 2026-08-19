package com.viettel.bccs.organization.staff.openapi;

public final class StaffControllerExamples {

    private StaffControllerExamples() {
    }

    public static final String STAFF_DTO_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000021",
              "requestId": "req-0021",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "staffId": 12345,
                "staffCode": "NV_001",
                "name": "Nguyễn Văn A",
                "tel": "0909123456",
                "email": "nguyenvana@viettel.vn",
                "idNo": "001234567890",
                "status": "1",
                "statusName": "Dang hoat dong",
                "shopId": 12345,
                "shopCode": "VTST_HN_001",
                "channelTypeId": 1,
                "type": 1,
                "province": "HN",
                "district": "BD",
                "precinct": "P1",
                "areaCode": "HN"
              }
            }""";

    public static final String STAFF_DTO_WITH_CHANNEL_OF_SALE_POINT_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000025",
              "requestId": "req-0025",
              "timestamp": "2026-08-19T02:00:00Z",
              "data": {
                "staffId": 12345,
                "staffCode": "NV_001",
                "name": "Nguyễn Văn A",
                "tel": "0909123456",
                "email": "nguyenvana@viettel.vn",
                "idNo": "001234567890",
                "status": "1",
                "statusName": "Dang hoat dong",
                "shopId": 12345,
                "shopCode": "VTST_HN_001",
                "channelTypeId": 1,
                "type": 1,
                "province": "HN",
                "district": "BD",
                "precinct": "P1",
                "areaCode": "HN",
                "isChannelOfSalePoint": false
              }
            }""";

    public static final String STOCK_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000022",
              "requestId": "req-0022",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "stockId": 1,
                  "code": "STOCK_001",
                  "name": "Kho hàng hóa Hà Nội",
                  "type": "1"
                }
              ]
            }""";

    public static final String STAFF_SHOP_FULL_INFO_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000023",
              "requestId": "req-0023",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "staffId": 12345,
                "staffCode": "NV_001",
                "name": "Nguyễn Văn A",
                "tel": "0909123456",
                "email": "nguyenvana@viettel.vn",
                "idNo": "001234567890",
                "idIssueDate": "2020-01-01",
                "idIssuePlace": "Hà Nội",
                "birthday": "1990-01-01",
                "address": "123 Nguyễn Trãi, Quận 1, TP HCM",
                "province": "HN",
                "district": "BD",
                "precinct": "P1",
                "shopId": 12345,
                "shopCode": "VTST_HN_001",
                "shopName": "Viettel Store Hà Nội",
                "status": "1",
                "statusName": "Dang hoat dong",
                "channelTypeId": 1,
                "channelTypeName": "Đại lý",
                "channelTypeCode": "CT01",
                "shopParentId": 10000,
                "shopParentCode": "VTST_HN",
                "shopParentName": "Viettel Hà Nội",
                "type": 1,
                "userId": 12345,
                "staffOwnerId": 12345,
                "staffOwnType": "OWNER",
                "areaCode": "HN",
                "shop": {
                  "shopId": 12345,
                  "name": "Viettel Store Hà Nội",
                  "shopCode": "VTST_HN_001",
                  "parentShopId": 10000,
                  "address": "123 Nguyễn Trãi, Quận 1, TP HCM",
                  "tel": "0909123456",
                  "email": "contact@viettel.vn",
                  "province": "HN",
                  "district": "BD",
                  "precinct": "P1",
                  "channelTypeId": 1,
                  "status": "1",
                  "shopPath": "/HN/VTST_HN_001",
                  "shopType": "1",
                  "areaCode": "HN",
                  "createDatetime": "2024-01-01T00:00:00.000+00:00",
                  "groupChannelTypeId": 5
                },
                "pointOfSale": "POS01"
              }
            }""";

    public static final String STAFF_SHOP_FULL_INFO_BY_ID_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000024",
              "requestId": "req-0024",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "staffId": 12345,
                "staffCode": "NV_001",
                "name": "Nguyễn Văn A",
                "tel": "0909123456",
                "email": "nguyenvana@viettel.vn",
                "idNo": "001234567890",
                "idIssueDate": "2020-01-01",
                "idIssuePlace": "Hà Nội",
                "birthday": "1990-01-01",
                "address": "123 Nguyễn Trãi, Quận 1, TP HCM",
                "province": "HN",
                "district": "BD",
                "precinct": "P1",
                "shopId": 12345,
                "shopCode": "VTST_HN_001",
                "shopName": "Viettel Store Hà Nội",
                "status": "1",
                "statusName": "Dang hoat dong",
                "channelTypeId": 1,
                "channelTypeName": "Đại lý",
                "type": 1,
                "userId": 12345,
                "staffOwnerId": 12345,
                "staffOwnType": "OWNER",
                "areaCode": "HN",
                "shop": {
                  "shopId": 12345,
                  "name": "Viettel Store Hà Nội",
                  "shopCode": "VTST_HN_001",
                  "parentShopId": 10000,
                  "address": "123 Nguyễn Trãi, Quận 1, TP HCM",
                  "tel": "0909123456",
                  "email": "contact@viettel.vn",
                  "province": "HN",
                  "district": "BD",
                  "precinct": "P1",
                  "channelTypeId": 1,
                  "status": "1",
                  "shopPath": "/HN/VTST_HN_001",
                  "shopType": "1",
                  "areaCode": "HN",
                  "createDatetime": "2024-01-01T00:00:00.000+00:00",
                  "groupChannelTypeId": 5
                },
                "pointOfSale": "POS01"
              }
            }""";

    public static final String CUST_TYPE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000024",
              "requestId": "req-0024",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "custType": "PREPAID",
                  "name": "Trả trước",
                  "createUser": "admin",
                  "createDatetime": "2024-01-01T00:00:00.000+00:00",
                  "updateUser": "admin",
                  "updateDatetime": "2024-06-01T00:00:00.000+00:00",
                  "description": "Khách hàng trả trước",
                  "status": "1",
                  "groupType": "1",
                  "tax": 10,
                  "plan": "1",
                  "representCust": "0",
                  "custTypeId": 1
                }
              ]
            }""";

}
