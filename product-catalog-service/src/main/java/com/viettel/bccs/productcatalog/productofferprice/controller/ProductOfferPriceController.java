package com.viettel.bccs.productcatalog.productofferprice.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceDTO;
import com.viettel.bccs.productcatalog.productofferprice.dto.response.ProductOfferPriceResponse;
import com.viettel.bccs.productcatalog.productofferprice.service.ProductOfferPriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-catalog-service/v1/productofferprice")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Offer Price", description = "API quản lý giá bán thiết bị")
public class ProductOfferPriceController {

    private final ProductOfferPriceService productOfferPriceService;

    private static final String PRICE_DTO_EXAMPLE = """
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

    private static final String PRICE_DTO_LIST_EXAMPLE = """
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

    private static final String PRICE_RESPONSE_LIST_EXAMPLE = """
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

    @GetMapping("/getById")
    @Operation(
            operationId = "getProductOfferPriceById",
            summary = "Lấy thông tin giá bán thiết bị theo ID",
            description = "Truy vấn chi tiết giá bán thiết bị (PRODUCT_OFFER_PRICE) theo productOfferPriceId. Trả về đầy đủ thông tin bao gồm giá, VAT, thời gian hiệu lực, chính sách giá và các trường audit.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRICE_DTO_EXAMPLE)))
    })
    public StandardResponse<ProductOfferPriceDTO> getById(
            @Parameter(description = "ID giá bán thiết bị", example = "12345", required = true)
            @RequestParam
            @Min(value = 1, message = "prodOfferPriceId phải >= 1")
            @Max(value = 999999999999999L, message = "prodOfferPriceId vượt quá độ dài cột (precision 15)")
            Long prodOfferPriceId) {
        return StandardResponses.success(productOfferPriceService.getById(prodOfferPriceId));
    }

    @PostMapping("/getPriceInServicesForPCCC")
    @Operation(
            operationId = "getPriceInServicesForPCCC",
            summary = "Lấy danh sách giá bán thiết bị phục vụ quản lý cước PCCC",
            description = "Truy vấn danh sách giá bán thiết bị theo các tiêu chí: ID/Code gói sản phẩm, loại sản phẩm, ID sản phẩm và chính sách giá. Phục vụ cho nghiệp vụ quản lý cước PCCC.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRICE_DTO_LIST_EXAMPLE)))
    })
    public StandardResponse<List<ProductOfferPriceDTO>> getPriceInServicesForPCCC(
            @Parameter(description = "ID gói sản phẩm", example = "123")
            @RequestParam(required = false)
            @Min(value = 1, message = "productPackageId phải >= 1")
            @Max(value = 9999999999L, message = "productPackageId vượt quá độ dài cột (precision 10)")
            Long productPackageId,

            @Parameter(description = "Mã gói sản phẩm", example = "PKG_001")
            @RequestParam(required = false)
            @Size(max = 50, message = "productPackageCode tối đa 50 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "productPackageCode chỉ gồm chữ, số, '_' hoặc '-'")
            String productPackageCode,

            @Parameter(description = "ID loại sản phẩm", example = "1")
            @RequestParam(required = false)
            @Min(value = 1, message = "productOfferType phải >= 1")
            @Max(value = 9999999999L, message = "productOfferType vượt quá độ dài cột (precision 10)")
            Long productOfferType,

            @Parameter(description = "ID sản phẩm", example = "456")
            @RequestParam(required = false)
            @Min(value = 1, message = "productOfferId phải >= 1")
            @Max(value = 9999999999L, message = "productOfferId vượt quá độ dài cột (precision 10)")
            Long productOfferId,

            @Parameter(description = "ID chính sách giá", example = "7")
            @RequestParam(required = false)
            @Min(value = 1, message = "pricePolicy phải >= 1")
            @Max(value = 9999999999L, message = "pricePolicy vượt quá độ dài cột (precision 10)")
            Long pricePolicy) {
        return StandardResponses.success(productOfferPriceService.getPriceInServicesForPCCC(
                productPackageId, productPackageCode, productOfferType, productOfferId, pricePolicy));
    }

    @GetMapping("/getPriceInServices")
    @Operation(
            operationId = "getPriceInServices",
            summary = "Lấy danh sách giá bán thiết bị trong gói dịch vụ",
            description = "Truy vấn danh sách giá bán thiết bị (ProductOfferPrice) theo các tiêu chí: ID/Code gói sản phẩm, loại sản phẩm, ID sản phẩm và chính sách giá. " +
                    "Hỗ trợ tính giá thiết bị CAM theo loại (indoor/outdoor) và giá thiết bị thông thường. " +
                    "Kết quả bao gồm thông tin giá, tên sản phẩm, và giá thiết bị khuyến mãi.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRICE_RESPONSE_LIST_EXAMPLE)))
    })
    public StandardResponse<List<ProductOfferPriceResponse>> getPriceInServices(
            @Parameter(description = "ID gói sản phẩm", example = "123")
            @RequestParam(required = false)
            @Min(value = 1, message = "productPackageId phải >= 1")
            @Max(value = 9999999999L, message = "productPackageId vượt quá độ dài cột (precision 10)")
            Long productPackageId,

            @Parameter(description = "Mã gói sản phẩm", example = "PKG_001")
            @RequestParam(required = false)
            @Size(max = 50, message = "productPackageCode tối đa 50 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "productPackageCode chỉ gồm chữ, số, '_' hoặc '-'")
            String productPackageCode,

            @Parameter(description = "ID loại sản phẩm", example = "1")
            @RequestParam(required = false)
            @Min(value = 1, message = "productOfferType phải >= 1")
            @Max(value = 9999999999L, message = "productOfferType vượt quá độ dài cột (precision 10)")
            Long productOfferType,

            @Parameter(description = "ID sản phẩm", example = "456")
            @RequestParam(required = false)
            @Min(value = 1, message = "productOfferId phải >= 1")
            @Max(value = 9999999999L, message = "productOfferId vượt quá độ dài cột (precision 10)")
            Long productOfferId,

            @Parameter(description = "ID chính sách giá", example = "7")
            @RequestParam(required = false)
            @Min(value = 1, message = "pricePolicy phải >= 1")
            @Max(value = 9999999999L, message = "pricePolicy vượt quá độ dài cột (precision 10)")
            Long pricePolicy) {
        return StandardResponses.success(productOfferPriceService.getPriceInServices(
                productPackageId, productPackageCode, productOfferType, productOfferId, pricePolicy));
    }

}
