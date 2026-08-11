package com.viettel.bccs.productcatalog.productpackage.openapi;

public final class ProductPackageControllerExamples {

    private ProductPackageControllerExamples() {
    }

    public static final String PRODUCT_PACKAGE_SINGLE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "productPackageId": 1001,
                "code": "PACKAGE_MOBILE_01",
                "name": "Goi cuoc di dong 01",
                "description": "Goi cuoc mau",
                "status": "1",
                "type": "2",
                "saleType": "1",
                "unit": "goi",
                "effectDatetime": "2026-01-01T00:00:00",
                "expireDatetime": null,
                "createDatetime": "2026-01-01T00:00:00",
                "updateDatetime": null,
                "createUser": "system",
                "updateUser": null,
                "version": "1",
                "accountingId": 1,
                "feeType": "1",
                "telecomServiceId": 1,
                "note": null,
                "note1": null,
                "note2": null,
                "areaGroupId": null,
                "ownerShopId": null,
                "sapMaterialNumber": null,
                "itTelcol": null
              }
            }""";

    public static final String PRODUCT_PACKAGE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "productPackageId": 1001,
                  "code": "PACKAGE_MOBILE_01",
                  "name": "Goi cuoc di dong 01",
                  "description": "Goi cuoc mau",
                  "status": "1",
                  "type": "2",
                  "saleType": "1",
                  "unit": "goi",
                  "effectDatetime": "2026-01-01T00:00:00",
                  "createDatetime": "2026-01-01T00:00:00",
                  "createUser": "system",
                  "version": "1",
                  "accountingId": 1,
                  "telecomServiceId": 1
                }
              ]
            }""";

    public static final String PACKAGE_CODES_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000003",
              "requestId": "req-0003",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": ["PACKAGE_MOBILE_01", "PACKAGE_MOBILE_02"]
            }""";

    public static final String SALE_SERVICE_ADV_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000004",
              "requestId": "req-0004",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "success": true,
                "saleService": {
                  "productPackageId": 1001,
                  "code": "SALE_SVC_01",
                  "name": "Dich vu ban hang 01",
                  "type": "2",
                  "status": "1"
                },
                "listSaleServiceModel": [],
                "listSaleServicePrice": [],
                "lstProductSpecCharDTO": [],
                "listProductOfferType": [],
                "isTLV": false,
                "isBonus": true,
                "removePackage": false,
                "shopIds": [],
                "specShopList": []
              }
            }""";

    public static final String SALE_SERVICE_INFO_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000005",
              "requestId": "req-0005",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "productPackageId": 1001,
                "code": "SALE_SVC_01",
                "name": "Dich vu ban hang 01",
                "type": "2",
                "status": "1",
                "telecomServiceId": 1,
                "listProdPackType": []
              }
            }""";
}
