package com.viettel.bccs.organization.stockchannelmapping.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.stockchannelmapping.dto.response.StockChannelMappingResponse;
import com.viettel.bccs.organization.stockchannelmapping.openapi.ApiFindActive;
import com.viettel.bccs.organization.stockchannelmapping.openapi.ApiFindByChannelType;
import com.viettel.bccs.organization.stockchannelmapping.service.StockChannelMappingService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/organization-resource-service/v1/stock-channel-mapping")
@RequiredArgsConstructor
@Validated
@Tag(name = "StockChannelMapping",
        description = "Mapping kho số chức năng - loại kênh > cửa hàng > user (STOCK_CHANNEL_MAPPING)")
public class StockChannelMappingController {

    private final StockChannelMappingService mappingService;

    @ApiFindActive
    @GetMapping("/findActive")
    public StandardResponse<List<StockChannelMappingResponse>> findActive() {
        return StandardResponses.success(mappingService.findActive());
    }

    @ApiFindByChannelType
    @GetMapping("/findByChannelType/{channelTypeId}")
    public StandardResponse<List<StockChannelMappingResponse>> findByChannelType(
            @Parameter(description = "ID loại kênh (CHANNEL_TYPE_ID)", example = "2", required = true)
            @PathVariable
            @Min(value = 0, message = "channelTypeId phải >= 0")
            @Max(value = 9999999999L, message = "channelTypeId vượt quá độ dài cột (precision 10)")
            Long channelTypeId) {
        return StandardResponses.success(mappingService.findByChannelType(channelTypeId));
    }


}
