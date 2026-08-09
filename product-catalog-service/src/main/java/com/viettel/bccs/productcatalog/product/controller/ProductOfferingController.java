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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-catalog-service/v1/product")
@RequiredArgsConstructor
@Tag(name = "Product Offering", description = "API quản lý sản phẩm gói cước")
public class ProductOfferingController {

    private final ProductOfferingService productOfferingService;
    private final ProductOfferCharUseService productOfferCharUseService;

    @GetMapping("/getByProductCode")
    public StandardResponse<ProductOfferingResponse> getByProductCode(@RequestParam String productCode) {
        return StandardResponses.success(productOfferingService.getByProductCode(productCode));
    }

    @GetMapping("/getListOfferAlterStatus")
    public StandardResponse<List<ProductOfferingDTO>> getListOfferAlterStatus(
            @RequestParam Long offerId,
            @RequestParam String changeChannel,
            @RequestParam boolean checkStatus) {
        return StandardResponses.success(productOfferingService.getListOfferAlterStatus(offerId, changeChannel, checkStatus));
    }

    @GetMapping("/findByTelecomSubTypeOfferTypeCheckProductStatus")
    @Operation(
            operationId = "findByTelecomSubTypeOfferTypeCheckProductStatus",
            summary = "Tìm kiếm sản phẩm theo dịch vụ viễn thông, loại thuê bao và loại sản phẩm",
            description = "Tìm kiếm sản phẩm gói cước theo các tiêu chí: ID dịch vụ viễn thông, loại thuê bao, loại sản phẩm và tùy chọn lấy sản phẩm đang active. Kết quả được sắp xếp theo mã sản phẩm."
    )
    public StandardResponse<List<ProductOfferingDTO>> findByTelecomSubTypeOfferTypeCheckProductStatus(
            @Parameter(description = "ID dịch vụ viễn thông", example = "1")
            @RequestParam(required = false) Long telecomServiceId,
            @Parameter(description = "Loại thuê bao (1: trả sau, 2: trả trước)", example = "1")
            @RequestParam(required = false) String subType,
            @Parameter(description = "ID loại sản phẩm", example = "1")
            @RequestParam(required = false) Long offerTypeId,
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
    public StandardResponse<List<ProductOfferingDTO>> findByCodeOrId(
            @Parameter(description = "ID sản phẩm", example = "12345")
            @RequestParam(required = false) Long proOfferId,
            @Parameter(description = "Mã sản phẩm", example = "PACKAGE_001")
            @RequestParam(required = false) String prodOfferCode,
            @Parameter(description = "Trạng thái sản phẩm", example = "1")
            @RequestParam(required = false) String status) {
        return StandardResponses.success(productOfferingService.findByCodeOrId(proOfferId, prodOfferCode, status));
    }

    @PostMapping("/findByPayTypeWithSpec")
    @Operation(
            operationId = "API_DAUNOI_TT_PRODUCT_019",
            summary = "Tìm kiếm sản phẩm theo loại thuê bao (payType), loại sản phẩm và đặc tính sản phẩm",
            description = "Tìm kiếm sản phẩm gói cước theo loại thuê bao (trả trước/trả sau), loại sản phẩm, tùy chọn lọc theo đặc tính sản phẩm (spec char). Kết quả chỉ bao gồm sản phẩm có status = 1."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(schema = @Schema(implementation = ProductOfferingDTO.class))),
            @ApiResponse(responseCode = "400", description = "Tham số bắt buộc bị thiếu hoặc không hợp lệ")
    })
    public StandardResponse<List<ProductOfferingDTO>> findByPayTypeWithSpec(
            @Parameter(description = "ID dịch vụ viễn thông (telecom service)", example = "1")
            @RequestParam(required = false) String telecomServiceId,

            @Parameter(description = "Loại thuê bao (payType) - bắt buộc. Ví dụ: 1 (trả trước), 2 (trả sau)", example = "1", required = true)
            @RequestParam String payType,

            @Parameter(description = "ID loại sản phẩm (product offer type) - bắt buộc", example = "1", required = true)
            @RequestParam String productOfferTypeId,

            @Parameter(description = "Danh sách điều kiện lọc theo đặc tính sản phẩm (spec char)")
            @RequestBody(required = false) List<FilterRequest> listProductSpec) {
        return StandardResponses.success(productOfferingService.findByPayTypeWithSpec(telecomServiceId, payType, productOfferTypeId, listProductSpec));
    }

    @GetMapping("/checkAttProductOrVasByCode")
    @Operation(
            summary = "Kiểm tra sản phẩm/VAS có đặc tính theo mã",
            description = "Kiểm tra sản phẩm hoặc VAS (xác định bởi productCode + productType là product_offer_type_id) có gán đặc tính (product_spec_char) theo attributeCode hay không. Chỉ tính các bản ghi đang active (status='1')."
    )
    public StandardResponse<Boolean> checkAttProductOrVasByCode(
            @Parameter(description = "Mã sản phẩm / VAS", example = "300", required = true)
            @RequestParam String productCode,

            @Parameter(description = "ID loại sản phẩm (product_offer_type_id) - dùng để phân biệt product/VAS", example = "1", required = true)
            @RequestParam String productType,

            @Parameter(description = "Mã đặc tính cần kiểm tra (product_spec_char.code)", example = "IS_CONNECTED", required = true)
            @RequestParam String attributeCode) {
        return StandardResponses.success(productOfferingService.checkAttProductOrVasByCode(productCode, productType, attributeCode));
    }

    @PostMapping("/findByCodesAndProductOfferType")
    @Operation(
            operationId = "API_DAUNOI_TT_PRODUCT_017",
            summary = "Tìm kiếm sản phẩm theo danh sách mã và loại sản phẩm",
            description = "Tìm kiếm sản phẩm gói cước theo danh sách mã sản phẩm (code) và loại sản phẩm (productOfferTypeId). Chỉ trả về sản phẩm có status = 1. Kết quả được sắp xếp theo mã sản phẩm."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công", content = @Content(schema = @Schema(implementation = ProductOfferingDTO.class))),
            @ApiResponse(responseCode = "400", description = "Tham số bắt buộc bị thiếu hoặc không hợp lệ")
    })
    public StandardResponse<List<ProductOfferingDTO>> findByCodesAndProductOfferType(
            @Parameter(description = "Danh sách mã sản phẩm cần tìm", example = "[\"CODE_001\", \"CODE_002\"]")
            @RequestBody List<String> codes,

            @Parameter(description = "ID loại sản phẩm (product offer type)", example = "1", required = true)
            @RequestParam Long productOfferTypeId) {
        return StandardResponses.success(productOfferingService.findByCodesAndProductOfferType(codes, productOfferTypeId));
    }

    @PostMapping("/findByIds")
    @Operation(
            operationId = "findByIds",
            summary = "Tìm kiếm sản phẩm theo danh sách ID",
            description = "Tìm kiếm sản phẩm gói cước theo danh sách productOfferingId. Trả về danh sách rỗng nếu không truyền id nào."
    )
    public StandardResponse<List<ProductOfferingDTO>> findByIds(
            @Parameter(description = "Danh sách ID sản phẩm cần tìm", example = "[12345, 67890]")
            @RequestBody List<Long> offerIds) {
        return StandardResponses.success(productOfferingService.findByIds(offerIds));
    }

    @GetMapping("/getListPricePlanByOfferId")
    @Operation(
            operationId = "getListPricePlanByOfferId",
            summary = "Lấy danh sách đặc tính giá cước (price plan) của sản phẩm",
            description = "Truy vấn các đặc tính (product_spec_char) thuộc nhóm giá cước (CHAR_TYPE = price plan) đang được gán (product_offer_char_use) cho một product offering, kèm giá trị (product_spec_char_value) tương ứng."
    )
    public StandardResponse<List<ProductSpecCharDTO>> getListPricePlanByOfferId(
            @Parameter(description = "ID sản phẩm", example = "500001", required = true)
            @RequestParam Long productOfferingId) {
        return StandardResponses.success(productOfferCharUseService.getListPricePlanByOfferId(productOfferingId));
    }

    @GetMapping("/getListVas")
    @Operation(
            operationId = "getListVas",
            summary = "Lấy danh sách VAS khả dụng cho 1 sản phẩm chính",
            description = "Trả về danh sách VAS (dịch vụ giá trị gia tăng) được gán cho sản phẩm chính qua bảng quan hệ PRODUCT_OFFER_RELATION, kèm thuộc tính và thông tin quan hệ, cùng typeIndex đánh dấu nhóm VAS loại trừ lẫn nhau (cấu hình qua OptionSet VAS_EXCLUSIVE_GROUP) — VAS cùng typeIndex chỉ được chọn tối đa 1."
    )
    public StandardResponse<List<ProductOfferingDTO>> getListVas(
            @Parameter(description = "ID sản phẩm chính", example = "500001", required = true)
            @RequestParam Long offerId) {
        return StandardResponses.success(productOfferingService.getListVas(offerId));
    }
}