package com.viettel.bccs.productcatalog.productpackage.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageDTO;
import com.viettel.bccs.productcatalog.productpackage.dto.response.ProductPackageResponse;
import com.viettel.bccs.productcatalog.productpackage.dto.response.SaleServiceAdvanceDTO;
import com.viettel.bccs.productcatalog.productpackage.service.ProductPackageService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Product Package", description = "APIs quản lý gói sản phẩm")
@RestController
@RequestMapping("/product-catalog-service/v1/product-package")
@RequiredArgsConstructor
@Validated
public class ProductPackageController {

    private final ProductPackageService service;

    private static final String PRODUCT_PACKAGE_SINGLE_EXAMPLE = """
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

    private static final String PRODUCT_PACKAGE_LIST_EXAMPLE = """
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

    private static final String PACKAGE_CODES_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000003",
              "requestId": "req-0003",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": ["PACKAGE_MOBILE_01", "PACKAGE_MOBILE_02"]
            }""";

    private static final String SALE_SERVICE_ADV_EXAMPLE = """
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

    private static final String SALE_SERVICE_INFO_EXAMPLE = """
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

    @Operation(operationId = "findProductPackageById", summary = "Lấy thông tin gói sản phẩm theo ID",
            description = "Tra cứu 1 bản ghi PRODUCT_PACKAGE theo khoá chính PRODUCT_PACKAGE_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_PACKAGE_SINGLE_EXAMPLE)))
    })
    @GetMapping("/findById/{id}")
    public StandardResponse<ProductPackageResponse> findById(
            @Parameter(description = "ID gói sản phẩm (PRODUCT_PACKAGE_ID)", example = "1001", required = true)
            @PathVariable
            @Min(value = 1, message = "id phải >= 1")
            @Max(value = 9999999999L, message = "id vượt quá độ dài cho phép")
            Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @Operation(
            operationId = "getProductPackageByCode",
            summary = "Lấy thông tin gói sản phẩm theo mã gói",
            description = "Truy vấn thông tin gói sản phẩm (product package) theo mã gói. Trả về danh sách vì một mã gói có thể map với nhiều bản ghi (theo telecomServiceId khác nhau). Chỉ trả về bản ghi có `status = 1`."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_PACKAGE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByCode/{code}")
    public StandardResponse<List<ProductPackageResponse>> getByCode(
            @Parameter(description = "Mã gói sản phẩm (product_package.code)", example = "PACKAGE_MOBILE_01", required = true)
            @PathVariable
            @Size(min = 1, max = 50, message = "code tối đa 50 ký tự")
            @Pattern(regexp = "^[a-zA-Z0-9_]{1,50}$", message = "code chỉ gồm chữ, số hoặc '_'")
            String code) {
        return StandardResponses.success(service.findByCode(code));
    }

    @Operation(operationId = "getProductPackageByStatus", summary = "Lấy danh sách gói sản phẩm theo trạng thái",
            description = "Truy vấn danh sách PRODUCT_PACKAGE theo cột STATUS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_PACKAGE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByStatus")
    public StandardResponse<List<ProductPackageResponse>> getByStatus(
            @Parameter(description = "Trạng thái (0/1)", example = "1", required = true)
            @RequestParam
            @Size(min = 1, max = 1, message = "status đúng 1 ký tự")
            @Pattern(regexp = "^[01]$", message = "status chỉ nhận giá trị 0 hoặc 1")
            String status) {
        return StandardResponses.success(service.findByStatus(status));
    }

    @Operation(operationId = "getProductPackageByType", summary = "Lấy danh sách gói sản phẩm theo loại",
            description = "Truy vấn danh sách PRODUCT_PACKAGE theo cột TYPE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_PACKAGE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByType")
    public StandardResponse<List<ProductPackageResponse>> getByType(
            @Parameter(description = "Loại gói sản phẩm (1: hàng hoá, 2: dịch vụ bán hàng)", example = "2", required = true)
            @RequestParam
            @Size(min = 1, max = 1, message = "type đúng 1 ký tự")
            @Pattern(regexp = "^[12]$", message = "type chỉ nhận giá trị 1 hoặc 2")
            String type) {
        return StandardResponses.success(service.findByType(type));
    }

    @Operation(operationId = "getProductPackageByTelecomServiceId", summary = "Lấy danh sách gói sản phẩm theo ID dịch vụ viễn thông",
            description = "Truy vấn danh sách PRODUCT_PACKAGE theo cột TELECOM_SERVICE_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_PACKAGE_LIST_EXAMPLE)))
    })
    @GetMapping("/getByTelecomServiceId")
    public StandardResponse<List<ProductPackageResponse>> getByTelecomServiceId(
            @Parameter(description = "ID dịch vụ viễn thông (TELECOM_SERVICE_ID)", example = "1", required = true)
            @RequestParam
            @Min(value = 1, message = "telecomServiceId phải >= 1")
            @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cho phép")
            Long telecomServiceId) {
        return StandardResponses.success(service.findByTelecomServiceId(telecomServiceId));
    }

    @Operation(operationId = "getPackageCodesByProductOfferTypeCount", summary = "Lấy danh sách mã gói theo số lượng loại mặt hàng",
            description = "Truy vấn danh sách mã gói sản phẩm (product_package.code), loại trừ loại mặt hàng excludeProdOfferType, tuỳ chọn lọc theo số lượng loại mặt hàng (pNumber).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PACKAGE_CODES_EXAMPLE)))
    })
    @GetMapping("/getPackageCodesByProductOfferTypeCount")
    public StandardResponse<List<String>> getPackageCodesByProductOfferTypeCount(
            @Parameter(description = "Loại mặt hàng cần loại trừ", example = "VAS", required = true)
            @RequestParam
            @Size(min = 1, max = 50, message = "excludeProdOfferType tối đa 50 ký tự")
            @Pattern(regexp = "^[^\\x00-\\x1F\\x7F]{1,50}$", message = "excludeProdOfferType không được chứa ký tự điều khiển")
            String excludeProdOfferType,
            @Parameter(description = "Số lượng loại mặt hàng cần lọc")
            @RequestParam(required = false)
            @Min(value = 0, message = "pNumber phải >= 0")
            @Max(value = 999, message = "pNumber tối đa 999")
            Integer pNumber) {
        return StandardResponses.success(service.findPackageCodesByProductOfferTypeCount(excludeProdOfferType, pNumber));
    }

    @Operation(operationId = "API_DAUNOI_TT_PRODUCT_011",
            summary = "API lấy thông tin dịch vụ bán hàng",
            description = "API lấy thông tin dịch vụ bán hàng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SALE_SERVICE_ADV_EXAMPLE)))
    })
    @GetMapping("/getSaleServicesAdvBOBySSCode")
    public StandardResponse<SaleServiceAdvanceDTO> getSaleServicesAdvBOBySSCode(
            @Parameter(description = "Mã dịch vụ bán hàng", example = "SALE_SVC_01", required = true)
            @RequestParam
            @Size(min = 1, max = 50, message = "saleServiceCode tối đa 50 ký tự")
            @Pattern(regexp = "^[a-zA-Z0-9_]{1,50}$", message = "saleServiceCode chỉ gồm chữ, số hoặc '_'")
            String saleServiceCode) {
        return StandardResponses.success(service.getSaleServicesAdvBOBySSCode(saleServiceCode));
    }

    @Operation(operationId = "API_DAUNOI_TT_PRODUCT_012",
            summary = "Lấy thông tin dịch vụ bán theo mã lý do",
            description = "Lấy thông tin dịch vụ bán theo mã lý do")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SALE_SERVICE_INFO_EXAMPLE)))
    })
    @GetMapping("/getSaleServiceInfo")
    public StandardResponse<ProductPackageDTO> getSaleServiceInfo(
            @Parameter(description = "Id lý do", example = "1", required = true)
            @RequestParam
            @Min(value = 1, message = "reasonId phải >= 1")
            @Max(value = 9999999999L, message = "reasonId vượt quá độ dài cho phép")
            Long reasonId,
            @Parameter(description = "Mã nhân viên - dùng để kiểm tra tồn kho theo cửa hàng khi loại mặt hàng yêu cầu checkShopStock")
            @RequestParam(required = false)
            @Size(max = 50, message = "staffCode tối đa 50 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9._-]{0,50}$", message = "staffCode chỉ gồm chữ, số, '.', '_' hoặc '-'")
            String staffCode) {
        return StandardResponses.success(service.getSaleServiceInfo(reasonId, staffCode));
    }
}