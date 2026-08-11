package com.viettel.bccs.productcatalog.product.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.common.dto.FilterRequest;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingDTO;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingResponse;
import com.viettel.bccs.productcatalog.product.service.ProductOfferingService;
import com.viettel.bccs.productcatalog.productoffercharuse.dto.response.ProductSpecCharDTO;
import com.viettel.bccs.productcatalog.productoffercharuse.service.ProductOfferCharUseService;
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
@RequestMapping("/product-catalog-service/v1/product")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Offering", description = "API quản lý sản phẩm gói cước")
public class ProductOfferingController {

    private final ProductOfferingService productOfferingService;
    private final ProductOfferCharUseService productOfferCharUseService;

    private static final String PRODUCT_SINGLE_EXAMPLE = """
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

    private static final String PRODUCT_LIST_EXAMPLE = """
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

    private static final String BOOLEAN_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000003",
              "requestId": "req-0003",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": true
            }""";

    private static final String SPEC_CHAR_LIST_EXAMPLE = """
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

    @Operation(operationId = "getByProductCode", summary = "Lấy sản phẩm theo mã",
            description = "Tra cứu 1 bản ghi PRODUCT_OFFERING theo CODE (mã sản phẩm). Ném lỗi BCCS-CATALOG-PRODUCT-0001 nếu không tìm thấy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_SINGLE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy sản phẩm với mã tương ứng")
    })
    @GetMapping("/getByProductCode")
    public StandardResponse<ProductOfferingResponse> getByProductCode(
            @Parameter(description = "Mã sản phẩm", example = "PACKAGE_001", required = true)
            @RequestParam
            @Size(min = 1, max = 50, message = "productCode tối đa 50 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
            String productCode) {
        return StandardResponses.success(productOfferingService.getByProductCode(productCode));
    }

    @Operation(operationId = "getListOfferAlterStatus", summary = "Lấy danh sách sản phẩm thay đổi trạng thái theo kênh",
            description = "Trả về danh sách sản phẩm chính (PRODUCT_OFFERING) đang active có quan hệ bán kèm (PRODUCT_OFFER_RELATION, relation_type_id=3) với offerId, qua đặc tính CHANGE_METHOD khớp changeChannel. checkStatus=true sẽ lọc thêm các bản ghi quan hệ/đặc tính đang active (status='1').")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_LIST_EXAMPLE)))
    })
    @GetMapping("/getListOfferAlterStatus")
    public StandardResponse<List<ProductOfferingDTO>> getListOfferAlterStatus(
            @Parameter(description = "ID sản phẩm chính", example = "500001", required = true)
            @RequestParam
            @Min(value = 1, message = "offerId phải >= 1")
            @Max(value = 9999999999L, message = "offerId vượt quá độ dài cột (precision 10)")
            Long offerId,

            @Parameter(description = "Kênh thay đổi (CHANGE_METHOD)", example = "ONLINE", required = true)
            @RequestParam
            @Size(min = 1, max = 100, message = "changeChannel tối đa 100 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,100}$", message = "changeChannel chỉ gồm chữ, số, '_' hoặc '-'")
            String changeChannel,

            @Parameter(description = "Chỉ lấy bản ghi quan hệ/đặc tính đang active", example = "true")
            @RequestParam boolean checkStatus) {
        return StandardResponses.success(productOfferingService.getListOfferAlterStatus(offerId, changeChannel, checkStatus));
    }

    @GetMapping("/findByTelecomSubTypeOfferTypeCheckProductStatus")
    @Operation(
            operationId = "findByTelecomSubTypeOfferTypeCheckProductStatus",
            summary = "Tìm kiếm sản phẩm theo dịch vụ viễn thông, loại thuê bao và loại sản phẩm",
            description = "Tìm kiếm sản phẩm gói cước theo các tiêu chí: ID dịch vụ viễn thông, loại thuê bao, loại sản phẩm và tùy chọn lấy sản phẩm đang active. Kết quả được sắp xếp theo mã sản phẩm."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_LIST_EXAMPLE)))
    })
    public StandardResponse<List<ProductOfferingDTO>> findByTelecomSubTypeOfferTypeCheckProductStatus(
            @Parameter(description = "ID dịch vụ viễn thông", example = "1")
            @RequestParam(required = false)
            @Min(value = 1, message = "telecomServiceId phải >= 1")
            @Max(value = 9999999999L, message = "telecomServiceId vượt quá độ dài cột (precision 10)")
            Long telecomServiceId,
            @Parameter(description = "Loại thuê bao (1: trả sau, 2: trả trước)", example = "1")
            @RequestParam(required = false)
            @Size(max = 1, message = "subType đúng 1 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "subType chỉ gồm chữ hoặc số")
            String subType,
            @Parameter(description = "ID loại sản phẩm", example = "1")
            @RequestParam(required = false)
            @Min(value = 1, message = "offerTypeId phải >= 1")
            @Max(value = 9999999999L, message = "offerTypeId vượt quá độ dài cột (precision 10)")
            Long offerTypeId,
            @Parameter(description = "Lấy sản phẩm đang active (status='1')", example = "true")
            @RequestParam boolean getActiveProduct) {
        return StandardResponses.success(productOfferingService.findByTelecomSubTypeOfferTypeCheckProductStatus(telecomServiceId, subType, offerTypeId, getActiveProduct));
    }

    @GetMapping("/findByCodeOrId")
    @Operation(
            operationId = "findByCodeOrId",
            summary = "Tìm kiếm sản phẩm theo mã hoặc ID",
            description = "Tìm kiếm sản phẩm gói cước theo ID hoặc mã sản phẩm, có tùy chọn lọc theo trạng thái. Trả về danh sách rỗng nếu không truyền cả proOfferId và prodOfferCode. Kết quả được sắp xếp theo mã sản phẩm."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_LIST_EXAMPLE)))
    })
    public StandardResponse<List<ProductOfferingDTO>> findByCodeOrId(
            @Parameter(description = "ID sản phẩm", example = "12345")
            @RequestParam(required = false)
            @Min(value = 1, message = "proOfferId phải >= 1")
            @Max(value = 9999999999L, message = "proOfferId vượt quá độ dài cột (precision 10)")
            Long proOfferId,
            @Parameter(description = "Mã sản phẩm", example = "PACKAGE_001")
            @RequestParam(required = false)
            @Size(max = 50, message = "prodOfferCode tối đa 50 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "prodOfferCode chỉ gồm chữ, số, '_' hoặc '-'")
            String prodOfferCode,
            @Parameter(description = "Trạng thái sản phẩm", example = "1")
            @RequestParam(required = false)
            @Size(max = 1, message = "status đúng 1 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9]{0,1}$", message = "status chỉ gồm chữ hoặc số")
            String status) {
        return StandardResponses.success(productOfferingService.findByCodeOrId(proOfferId, prodOfferCode, status));
    }

    @PostMapping("/findByPayTypeWithSpec")
    @Operation(
            operationId = "API_DAUNOI_TT_PRODUCT_019",
            summary = "Tìm kiếm sản phẩm theo loại thuê bao (payType), loại sản phẩm và đặc tính sản phẩm",
            description = "Tìm kiếm sản phẩm gói cước theo loại thuê bao (trả trước/trả sau), loại sản phẩm, tùy chọn lọc theo đặc tính sản phẩm (spec char). Kết quả chỉ bao gồm sản phẩm có status = 1."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_LIST_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "Tham số bắt buộc bị thiếu hoặc không hợp lệ")
    })
    public StandardResponse<List<ProductOfferingDTO>> findByPayTypeWithSpec(
            @Parameter(description = "ID dịch vụ viễn thông (telecom service)", example = "1")
            @RequestParam(required = false)
            @Size(max = 50, message = "telecomServiceId tối đa 50 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,50}$", message = "telecomServiceId chỉ gồm chữ, số, '_' hoặc '-'")
            String telecomServiceId,

            @Parameter(description = "Loại thuê bao (payType) - bắt buộc. Ví dụ: 1 (trả trước), 2 (trả sau)", example = "1", required = true)
            @RequestParam
            @Size(min = 1, max = 1, message = "payType đúng 1 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9]{1}$", message = "payType chỉ gồm chữ hoặc số")
            String payType,

            @Parameter(description = "ID loại sản phẩm (product offer type) - bắt buộc", example = "1", required = true)
            @RequestParam
            @Size(min = 1, max = 50, message = "productOfferTypeId tối đa 50 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "productOfferTypeId chỉ gồm chữ, số, '_' hoặc '-'")
            String productOfferTypeId,

            @Parameter(description = "Danh sách điều kiện lọc theo đặc tính sản phẩm (spec char)")
            @RequestBody(required = false)
            @Size(max = 100, message = "listProductSpec tối đa 100 phần tử")
            List<FilterRequest> listProductSpec) {
        return StandardResponses.success(productOfferingService.findByPayTypeWithSpec(telecomServiceId, payType, productOfferTypeId, listProductSpec));
    }

    @GetMapping("/checkAttProductOrVasByCode")
    @Operation(
            summary = "Kiểm tra sản phẩm/VAS có đặc tính theo mã",
            description = "Kiểm tra sản phẩm hoặc VAS (xác định bởi productCode + productType là product_offer_type_id) có gán đặc tính (product_spec_char) theo attributeCode hay không. Chỉ tính các bản ghi đang active (status='1')."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = BOOLEAN_EXAMPLE)))
    })
    public StandardResponse<Boolean> checkAttProductOrVasByCode(
            @Parameter(description = "Mã sản phẩm / VAS", example = "300", required = true)
            @RequestParam
            @Size(min = 1, max = 50, message = "productCode tối đa 50 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
            String productCode,

            @Parameter(description = "ID loại sản phẩm (product_offer_type_id) - dùng để phân biệt product/VAS", example = "1", required = true)
            @RequestParam
            @Size(min = 1, max = 50, message = "productType tối đa 50 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "productType chỉ gồm chữ, số, '_' hoặc '-'")
            String productType,

            @Parameter(description = "Mã đặc tính cần kiểm tra (product_spec_char.code)", example = "IS_CONNECTED", required = true)
            @RequestParam
            @Size(min = 1, max = 200, message = "attributeCode tối đa 200 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,200}$", message = "attributeCode chỉ gồm chữ, số, '_' hoặc '-'")
            String attributeCode) {
        return StandardResponses.success(productOfferingService.checkAttProductOrVasByCode(productCode, productType, attributeCode));
    }

    @GetMapping("/hasProductAtt")
    @Operation(
            summary = "Kiểm tra sản phẩm có đặc tính theo mã",
            description = "Giống hệt checkAttProductOrVasByCode, chỉ khác: không lọc theo product_offer_type_id (không phân biệt product/VAS). Kiểm tra sản phẩm (xác định bởi productCode) có gán đặc tính (product_spec_char) theo attributeCode hay không. Chỉ tính các bản ghi đang active (status='1')."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = BOOLEAN_EXAMPLE)))
    })
    public StandardResponse<Boolean> hasProductAtt(
            @Parameter(description = "Mã sản phẩm / VAS", example = "300", required = true)
            @RequestParam
            @Size(min = 1, max = 50, message = "productCode tối đa 50 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "productCode chỉ gồm chữ, số, '_' hoặc '-'")
            String productCode,

            @Parameter(description = "Mã đặc tính cần kiểm tra (product_spec_char.code)", example = "IS_CONNECTED", required = true)
            @RequestParam
            @Size(min = 1, max = 200, message = "attributeCode tối đa 200 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,200}$", message = "attributeCode chỉ gồm chữ, số, '_' hoặc '-'")
            String attributeCode) {
        return StandardResponses.success(productOfferingService.hasProductAtt(productCode, attributeCode));
    }

    @PostMapping("/findByCodesAndProductOfferType")
    @Operation(
            operationId = "API_DAUNOI_TT_PRODUCT_017",
            summary = "Tìm kiếm sản phẩm theo danh sách mã và loại sản phẩm",
            description = "Tìm kiếm sản phẩm gói cước theo danh sách mã sản phẩm (code) và loại sản phẩm (productOfferTypeId). Chỉ trả về sản phẩm có status = 1. Kết quả được sắp xếp theo mã sản phẩm."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_LIST_EXAMPLE))),
            @ApiResponse(responseCode = "400", description = "Tham số bắt buộc bị thiếu hoặc không hợp lệ")
    })
    public StandardResponse<List<ProductOfferingDTO>> findByCodesAndProductOfferType(
            @Parameter(description = "Danh sách mã sản phẩm cần tìm", example = "[\"CODE_001\", \"CODE_002\"]")
            @RequestBody
            @Size(min = 1, max = 1000, message = "codes phải có từ 1 đến 1000 phần tử")
            List<String> codes,

            @Parameter(description = "ID loại sản phẩm (product offer type)", example = "1", required = true)
            @RequestParam
            @Min(value = 1, message = "productOfferTypeId phải >= 1")
            @Max(value = 9999999999L, message = "productOfferTypeId vượt quá độ dài cột (precision 10)")
            Long productOfferTypeId) {
        return StandardResponses.success(productOfferingService.findByCodesAndProductOfferType(codes, productOfferTypeId));
    }

    @PostMapping("/findByIds")
    @Operation(
            operationId = "findByIds",
            summary = "Tìm kiếm sản phẩm theo danh sách ID",
            description = "Tìm kiếm sản phẩm gói cước theo danh sách productOfferingId. Trả về danh sách rỗng nếu không truyền id nào."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_LIST_EXAMPLE)))
    })
    public StandardResponse<List<ProductOfferingDTO>> findByIds(
            @Parameter(description = "Danh sách ID sản phẩm cần tìm", example = "[12345, 67890]")
            @RequestBody
            @Size(min = 1, max = 1000, message = "offerIds phải có từ 1 đến 1000 phần tử")
            List<Long> offerIds) {
        return StandardResponses.success(productOfferingService.findByIds(offerIds));
    }

    @GetMapping("/getListPricePlanByOfferId")
    @Operation(
            operationId = "getListPricePlanByOfferId",
            summary = "Lấy danh sách đặc tính giá cước (price plan) của sản phẩm",
            description = "Truy vấn các đặc tính (product_spec_char) thuộc nhóm giá cước (CHAR_TYPE = price plan) đang được gán (product_offer_char_use) cho một product offering, kèm giá trị (product_spec_char_value) tương ứng."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SPEC_CHAR_LIST_EXAMPLE)))
    })
    public StandardResponse<List<ProductSpecCharDTO>> getListPricePlanByOfferId(
            @Parameter(description = "ID sản phẩm", example = "500001", required = true)
            @RequestParam
            @Min(value = 1, message = "productOfferingId phải >= 1")
            @Max(value = 9999999999L, message = "productOfferingId vượt quá độ dài cột (precision 10)")
            Long productOfferingId) {
        return StandardResponses.success(productOfferCharUseService.getListPricePlanByOfferId(productOfferingId));
    }

    @GetMapping("/getListVas")
    @Operation(
            operationId = "getListVas",
            summary = "Lấy danh sách VAS khả dụng cho 1 sản phẩm chính",
            description = "Trả về danh sách VAS (dịch vụ giá trị gia tăng) được gán cho sản phẩm chính qua bảng quan hệ PRODUCT_OFFER_RELATION, kèm thuộc tính và thông tin quan hệ, cùng typeIndex đánh dấu nhóm VAS loại trừ lẫn nhau (cấu hình qua OptionSet VAS_EXCLUSIVE_GROUP) — VAS cùng typeIndex chỉ được chọn tối đa 1."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_LIST_EXAMPLE)))
    })
    public StandardResponse<List<ProductOfferingDTO>> getListVas(
            @Parameter(description = "ID sản phẩm chính", example = "500001", required = true)
            @RequestParam
            @Min(value = 1, message = "offerId phải >= 1")
            @Max(value = 9999999999L, message = "offerId vượt quá độ dài cột (precision 10)")
            Long offerId) {
        return StandardResponses.success(productOfferingService.getListVas(offerId));
    }
}
