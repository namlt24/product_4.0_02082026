package com.viettel.bccs.organization.shop.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.shop.dto.ShopDTO;
import com.viettel.bccs.organization.shop.dto.response.StockCodeResponse;
import com.viettel.bccs.organization.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.viettel.bccs.organization.shop.openapi.ShopControllerExamples.*;

@RestController
@RequestMapping("/organization-resource-service/v1/shop")
@RequiredArgsConstructor
@Validated
@Tag(name = "Shop", description = "Tra cứu thông tin cửa hàng/đại lý (SHOP)")
public class ShopController {

    private final ShopService shopService;

    @Operation(operationId = "getActiveShopById", summary = "Lấy cửa hàng active theo ID",
            description = "Tra cứu 1 bản ghi SHOP có STATUS active theo SHOP_ID (khoá chính).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SHOP_SINGLE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy cửa hàng với id tương ứng")
    })
    @GetMapping("/getActiveById/{shopId}")
    public StandardResponse<ShopDTO> getActiveById(
            @Parameter(description = "ID cửa hàng (SHOP_ID)", example = "12345", required = true)
            @PathVariable
            @Min(value = 0, message = "shopId phải >= 0")
            @Max(value = 9999999999L, message = "shopId vượt quá độ dài cột (precision 10)")
            Long shopId) {
        return StandardResponses.success(shopService.getActiveById(shopId));
    }

    @Operation(operationId = "getActiveShopByShopCode", summary = "Lấy cửa hàng active theo mã cửa hàng",
            description = "Tra cứu 1 bản ghi SHOP có STATUS active theo SHOP_CODE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SHOP_SINGLE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy cửa hàng với mã tương ứng")
    })
    @GetMapping("/getActiveByShopCode/{shopCode}")
    public StandardResponse<ShopDTO> getActiveByShopCode(
            @Parameter(description = "Mã cửa hàng (SHOP_CODE)", example = "VTST_HN_001", required = true)
            @PathVariable
            @Size(max = 40, message = "shopCode tối đa 40 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "shopCode chỉ gồm chữ, số, '_' hoặc '-'")
            String shopCode) {
        return StandardResponses.success(shopService.getActiveByShopCode(shopCode));
    }

    @Operation(operationId = "getStockCode", summary = "Lấy mã kho theo chủ sở hữu",
            description = "Tra cứu mã kho (STOCK_CODE) theo ownerId và ownerType.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = STOCK_CODE_EXAMPLE)))
    })
    @GetMapping("/getStockCode")
    public StandardResponse<StockCodeResponse> getStockCode(
            @Parameter(description = "ID chủ sở hữu (shopId/staffId)", example = "12345", required = true)
            @RequestParam
            @Min(value = 0, message = "ownerId phải >= 0")
            @Max(value = Long.MAX_VALUE, message = "ownerId vượt quá giới hạn cho phép")
            Long ownerId,
            @Parameter(description = "Loại chủ sở hữu", example = "1", required = true)
            @RequestParam
            @Min(value = 0, message = "ownerType phải >= 0")
            @Max(value = 99, message = "ownerType vượt quá giới hạn cho phép")
            Integer ownerType) {
        return StandardResponses.success(shopService.getStockCode(ownerId, ownerType));
    }

    @Operation(operationId = "findActiveByShopIds",
            summary = "Tìm danh sách cửa hàng active theo nhiều shopId",
            description = "Truy vấn danh sách cửa hàng có status = 1 theo danh sách shopId. " +
                    "Query được chia batch (100 bản ghi/batch) để tránh lỗi ORA-01795 khi danh sách lớn.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = SHOP_LIST_EXAMPLE)))
    })
    @PostMapping("/findActiveByShopIds")
    public StandardResponse<List<ShopDTO>> findActiveByShopIds(
            @Parameter(description = "Danh sách ID cửa hàng cần truy vấn", required = true)
            @RequestBody
            @Size(min = 1, max = 1000, message = "shopIds phải có từ 1 đến 1000 phần tử")
            List<@Valid Long> shopIds) {
        return StandardResponses.success(shopService.findActiveByShopIds(shopIds));
    }
}
