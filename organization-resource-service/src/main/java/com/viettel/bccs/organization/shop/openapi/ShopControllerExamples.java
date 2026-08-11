package com.viettel.bccs.organization.shop.openapi;

public final class ShopControllerExamples {

    private ShopControllerExamples() {
    }

    public static final String SHOP_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000011",
              "requestId": "req-0011",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "shopId": 12345,
                "name": "Viettel Store Hà Nội",
                "parentShopId": 10000,
                "account": "1234567890",
                "bankName": "Vietcombank",
                "address": "123 Nguyễn Trãi, Quận 1, TP HCM",
                "tel": "0909123456",
                "fax": "02812345678",
                "shopCode": "VTST_HN_001",
                "shopType": "1",
                "contactName": "Nguyễn Văn A",
                "contactTitle": "Giám đốc",
                "telNumber": "0909123456",
                "email": "contact@viettel.vn",
                "description": "Cửa hàng Viettel Store",
                "province": "HN",
                "parShopCode": "VTST_HN",
                "centerCode": "1",
                "oldShopCode": null,
                "company": "Viettel Group",
                "tin": "0123456789",
                "shop": "Viettel",
                "provinceCode": "HN",
                "payComm": "Y",
                "createDate": "2024-01-01",
                "channelTypeId": 1,
                "discountPolicy": "DISC_001",
                "pricePolicy": "PRICE_001",
                "shopPath": "/HN/VTST_HN_001",
                "district": "Ba Đình",
                "precinct": "Phường 1",
                "areaCode": "HN",
                "idNo": "001234567890",
                "idIssuePlace": "Hà Nội",
                "idIssueDate": "2020-01-01",
                "streetBlock": "Phường 1",
                "street": "Nguyễn Trãi",
                "home": "123",
                "idType": 1,
                "shopPathName": "Hà Nội > Ba Đình > Viettel Store",
                "contractNo": "HD_2024_001",
                "fileName": "hop_dong.pdf",
                "businessLicence": "GPKD_001",
                "bankplusMobile": "0909123456",
                "stockNum": 100,
                "stockNumImp": 50,
                "updateDateTime": "2024-06-01",
                "bankCode": "VCB",
                "shopKeeperId": 999,
                "shopDirectorId": 888,
                "groupChannelTypeId": 5,
                "staffOwnerId": 100,
                "tenantId": 1,
                "businessProvince": "HN",
                "businessDistrict": "BD",
                "businessPrecinct": "P1",
                "businessStreetBlock": "Phường 1",
                "businessStreet": "Nguyễn Trãi",
                "businessHome": "456",
                "businessAreacode": "HN",
                "businessAddress": "456 Nguyễn Trãi, Ba Đình, Hà Nội",
                "createUser": "admin",
                "updateUser": "admin",
                "createDatetime": "2024-01-01T00:00:00.000+00:00",
                "status": "1",
                "turnover": "Y",
                "birthday": "1990-01-01"
              }
            }""";

    public static final String STOCK_CODE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000012",
              "requestId": "req-0012",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "stockCode": "STOCK_001"
              }
            }""";

    public static final String SHOP_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000013",
              "requestId": "req-0013",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "shopId": 12345,
                  "name": "Viettel Store Hà Nội",
                  "shopCode": "VTST_HN_001",
                  "status": "1"
                }
              ]
            }""";

}
