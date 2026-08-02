package com.viettel.bccs.productcatalog.product.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.common.dto.FilterRequest;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingDTO;
import com.viettel.bccs.productcatalog.product.dto.response.ProductOfferingResponse;
import com.viettel.bccs.productcatalog.product.service.ProductOfferingService;
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
}