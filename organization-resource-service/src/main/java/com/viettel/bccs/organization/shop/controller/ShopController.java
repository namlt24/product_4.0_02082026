package com.viettel.bccs.organization.shop.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.shop.dto.ShopDTO;
import com.viettel.bccs.organization.shop.dto.response.StockCodeResponse;
import com.viettel.bccs.organization.shop.openapi.ApiFindActiveByShopIds;
import com.viettel.bccs.organization.shop.openapi.ApiGetActiveById;
import com.viettel.bccs.organization.shop.openapi.ApiGetActiveByIdWithChannelOfAgent;
import com.viettel.bccs.organization.shop.openapi.ApiGetActiveByShopCode;
import com.viettel.bccs.organization.shop.openapi.ApiGetStockCode;
import com.viettel.bccs.organization.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization-resource-service/v1/shop")
@RequiredArgsConstructor
@Tag(name = "Shop", description = "Tra cứu thông tin cửa hàng/đại lý (SHOP)")
public class ShopController {

    private final ShopService shopService;

    @ApiGetActiveById
    @GetMapping("/getActiveById/{shopId}")
    public StandardResponse<ShopDTO> getActiveById(
            @Parameter(description = "ID cửa hàng (SHOP_ID)", example = "12345", required = true)
            @PathVariable
            Long shopId) {
        return StandardResponses.success(shopService.getActiveById(shopId));
    }

    @ApiGetActiveByIdWithChannelOfAgent
    @GetMapping("/getActiveByIdWithChannelOfAgent/{shopId}")
    public StandardResponse<ShopDTO> getActiveByIdWithChannelOfAgent(
            @Parameter(description = "ID cửa hàng (SHOP_ID)", example = "12345", required = true)
            @PathVariable
            Long shopId) {
        return StandardResponses.success(shopService.getActiveByIdWithChannelOfAgent(shopId));
    }

    @ApiGetActiveByShopCode
    @GetMapping("/getActiveByShopCode/{shopCode}")
    public StandardResponse<ShopDTO> getActiveByShopCode(
            @Parameter(description = "Mã cửa hàng (SHOP_CODE)", example = "VTST_HN_001", required = true)
            @PathVariable
            String shopCode) {
        return StandardResponses.success(shopService.getActiveByShopCode(shopCode));
    }

    @ApiGetStockCode
    @GetMapping("/getStockCode")
    public StandardResponse<StockCodeResponse> getStockCode(
            @Parameter(description = "ID chủ sở hữu (shopId/staffId)", example = "12345", required = true)
            @RequestParam(required = false)
            Long ownerId,
            @Parameter(description = "Loại chủ sở hữu", example = "1", required = true)
            @RequestParam(required = false)
            Integer ownerType) {
        return StandardResponses.success(shopService.getStockCode(ownerId, ownerType));
    }

    @ApiFindActiveByShopIds
    @PostMapping("/findActiveByShopIds")
    public StandardResponse<List<ShopDTO>> findActiveByShopIds(
            @Parameter(description = "Danh sách ID cửa hàng cần truy vấn", required = true)
            @RequestBody
            List<Long> shopIds) {
        return StandardResponses.success(shopService.findActiveByShopIds(shopIds));
    }
}
