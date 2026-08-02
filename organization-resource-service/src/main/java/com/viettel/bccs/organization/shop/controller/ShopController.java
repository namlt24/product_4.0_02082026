package com.viettel.bccs.organization.shop.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.shop.dto.ShopDTO;
import com.viettel.bccs.organization.shop.dto.response.StockCodeResponse;
import com.viettel.bccs.organization.shop.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization-resource-service/v1/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/getActiveById/{shopId}")
    public StandardResponse<ShopDTO> getActiveById(@PathVariable Long shopId) {
        return StandardResponses.success(shopService.getActiveById(shopId));
    }

    @GetMapping("/getActiveByShopCode/{shopCode}")
    public StandardResponse<ShopDTO> getActiveByShopCode(@PathVariable String shopCode) {
        return StandardResponses.success(shopService.getActiveByShopCode(shopCode));
    }

    @GetMapping("/getStockCode")
    public StandardResponse<StockCodeResponse> getStockCode(@RequestParam Long ownerId,
                                                            @RequestParam Integer ownerType) {
        return StandardResponses.success(shopService.getStockCode(ownerId, ownerType));
    }

    @PostMapping("/findActiveByShopIds")
    @Operation(
            summary = "Tìm danh sách cửa hàng active theo nhiều shopId",
            description = "Truy vấn danh sách cửa hàng có status = 1 theo danh sách shopId. " +
                    "Query được chia batch (100 bản ghi/batch) để tránh lỗi ORA-01795 khi danh sách lớn."
    )
    public StandardResponse<List<ShopDTO>> findActiveByShopIds(
            @Parameter(description = "Danh sách ID cửa hàng cần truy vấn", required = true)
            @RequestBody List<@Valid Long> shopIds) {
        return StandardResponses.success(shopService.findActiveByShopIds(shopIds));
    }
}