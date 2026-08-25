package com.viettel.bccs.productcatalog.productofferprice.openapi;

public final class ProductOfferPriceControllerExamples {

    private ProductOfferPriceControllerExamples() {
    }

    public static final String PLEDGE_PRICE_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000004",
              "requestId": "req-0004",
              "timestamp": "2026-08-20T02:00:00Z",
              "data": [
                {
                  "price": 1500000,
                  "pledgeAmount": 500000,
                  "pledgeTime": 12,
                  "priorPay": "3"
                }
              ]
            }""";

    public static final String PRICE_DTO_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000001",
              "requestId": "req-0001",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "productOfferPriceId": 12345,
                "productOfferingId": 456,
                "pricePolicyId": 7,
                "priceTypeId": 1,
                "name": "Giá bán lẻ thiết bị A",
                "description": "Giá bán lẻ thiết bị cho khách hàng",
                "price": 1500000,
                "vat": 10,
                "pledgeAmount": 500000,
                "pledgeTime": 12,
                "priorPay": 300000,
                "status": "1",
                "effectDatetime": "2026-01-01T00:00:00.000+00:00",
                "expireDatetime": "2026-12-31T00:00:00.000+00:00",
                "priority": 1,
                "effectType": "1",
                "cronExpression": null,
                "createUser": "admin",
                "createDatetime": "2026-01-01T00:00:00.000+00:00",
                "updateUser": "admin",
                "updateDatetime": "2026-06-15T00:00:00.000+00:00",
                "programCode": "PCCC_2026",
                "programMonth": 6,
                "isSelectAllShop": "0",
                "limited": 0,
                "productOfferName": "Camera AI Outdoor",
                "priceEquipment": null
              }
            }""";

    public static final String PRICE_DTO_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000002",
              "requestId": "req-0002",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "productOfferPriceId": 12345,
                  "productOfferingId": 456,
                  "pricePolicyId": 7,
                  "priceTypeId": 1,
                  "name": "Giá bán lẻ thiết bị A",
                  "description": "Giá bán lẻ thiết bị cho khách hàng",
                  "price": 1500000,
                  "vat": 10,
                  "pledgeAmount": 500000,
                  "pledgeTime": 12,
                  "priorPay": 300000,
                  "status": "1",
                  "effectDatetime": "2026-01-01T00:00:00.000+00:00",
                  "expireDatetime": "2026-12-31T00:00:00.000+00:00",
                  "priority": 1,
                  "effectType": "1",
                  "cronExpression": null,
                  "createUser": "admin",
                  "createDatetime": "2026-01-01T00:00:00.000+00:00",
                  "updateUser": "admin",
                  "updateDatetime": "2026-06-15T00:00:00.000+00:00",
                  "programCode": "PCCC_2026",
                  "programMonth": 6,
                  "isSelectAllShop": "0",
                  "limited": 0,
                  "productOfferName": "Camera AI Outdoor",
                  "priceEquipment": null
                }
              ]
            }""";

    public static final String PRICE_RESPONSE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000003",
              "requestId": "req-0003",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "productOfferPriceId": 12345,
                  "productOfferingId": 456,
                  "pricePolicyId": 7,
                  "priceTypeId": 1,
                  "name": "Giá bán lẻ thiết bị A",
                  "description": "Giá bán lẻ thiết bị cho khách hàng",
                  "price": 1500000,
                  "vat": 10,
                  "pledgeAmount": 500000,
                  "pledgeTime": 12,
                  "priorPay": 300000,
                  "status": "1",
                  "effectDatetime": "2026-01-01T00:00:00.000+00:00",
                  "expireDatetime": "2026-12-31T00:00:00.000+00:00",
                  "priority": 1,
                  "effectType": "1",
                  "cronExpression": null,
                  "createUser": "admin",
                  "createDatetime": "2026-01-01T00:00:00.000+00:00",
                  "updateUser": "admin",
                  "updateDatetime": "2026-06-15T00:00:00.000+00:00",
                  "programCode": "PCCC_2026",
                  "programMonth": 6,
                  "isSelectAllShop": "0",
                  "limited": 0,
                  "productOfferName": "Camera AI Outdoor",
                  "priceEquipment": 900000,
                  "priceEquipmentId": 12346,
                  "priceEquipmentTypeId": 2
                }
              ]
            }""";
}
