package com.viettel.bccs.policy.ref.refprodpackpotype.openapi;

public final class RefProdPackPoTypeControllerExamples {

    private RefProdPackPoTypeControllerExamples() {
    }

    public static final String REF_PROD_PACK_PO_TYPE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "prodPackTypeId": 1,
                  "productPackageId": 10,
                  "productOfferTypeId": 5,
                  "status": "1",
                  "updateStock": "1",
                  "checkStaffStock": "1",
                  "checkShopStock": "1",
                  "updateDatetime": "2024-06-15T00:00:00.000+00:00",
                  "limitGoods": 100,
                  "transferIm": "1"
                }
              ]
            }""";
}
