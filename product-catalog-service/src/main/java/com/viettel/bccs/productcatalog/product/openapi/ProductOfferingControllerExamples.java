package com.viettel.bccs.productcatalog.product.openapi;

public final class ProductOfferingControllerExamples {

    private ProductOfferingControllerExamples() {
    }

    public static final String PRODUCT_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "productOfferingId": 12345,
                "productSpecId": 100,
                "productOfferTypeId": 1,
                "name": "Gói cước data 50GB",
                "code": "PACKAGE_001",
                "subType": "1",
                "telecomServiceId": 1,
                "description": "Gói cước data tốc độ cao",
                "status": "1",
                "effectDatetime": "2026-01-01T00:00:00.000+00:00",
                "expireDatetime": null,
                "createUser": "system",
                "createDatetime": "2026-01-01T00:00:00.000+00:00",
                "updateUser": null,
                "updateDatetime": null,
                "version": "1.0",
                "checkSerial": 0,
                "checkDeposit": 0,
                "unit": "GB",
                "accountingModelCode": "ACC_MODEL_001",
                "accountingModelName": "Model chuẩn",
                "accountingName": "Kế toán mặc định",
                "accountingCode": "ACC_001",
                "demoDuration": null,
                "isDemo": 0,
                "deviceType": null,
                "transceiver": null,
                "stockModelType": null,
                "ownerShopId": null,
                "returnStockWhenCancelled": "N",
                "returnStockWhenCancelled1": "N",
                "sapMaterialNumber": null,
                "usageId": null,
                "distribute": "1",
                "numMonth": 1,
                "isBundle": "0",
                "sapProductType": "1"
              }
            }""";

    public static final String PRODUCT_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "code": "PACKAGE_001",
                  "name": "Gói cước data 50GB",
                  "productOfferingId": 12345,
                  "productOfferTypeId": 1,
                  "telecomServiceId": 1,
                  "status": "1",
                  "subType": "1",
                  "description": "Gói cước data tốc độ cao",
                  "unit": "GB",
                  "createUser": "system",
                  "createDatetime": "2026-01-01T00:00:00.000+00:00",
                  "price": 100000,
                  "listingPrice": 120000
                }
              ]
            }""";

    public static final String BOOLEAN_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000003",
              "requestId": "req-0003",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": true
            }""";

    public static final String SPEC_CHAR_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000004",
              "requestId": "req-0004",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "productSpecCharId": 12345,
                  "name": "Giá cước tháng",
                  "charType": "PRICE_PLAN",
                  "code": "MONTHLY_FEE",
                  "status": "1",
                  "productOfferingId": 500001,
                  "offerCharUseId": 222,
                  "productSpecCharValueDTO": {
                    "productSpecCharValueId": 111,
                    "value": "50000",
                    "status": "1"
                  }
                }
              ]
            }""";
}
