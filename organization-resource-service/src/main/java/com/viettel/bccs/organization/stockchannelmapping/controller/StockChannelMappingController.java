package com.viettel.bccs.organization.stockchannelmapping.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.stockchannelmapping.dto.response.StockChannelMappingResponse;
import com.viettel.bccs.organization.stockchannelmapping.service.StockChannelMappingService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.viettel.bccs.organization.stockchannelmapping.openapi.StockChannelMappingControllerExamples.*;

@RestController
@RequestMapping("/organization-resource-service/v1/stock-channel-mapping")
@RequiredArgsConstructor
@Validated
@Tag(name = "StockChannelMapping", description = "Mapping kho số chức năng - loại kênh > cửa hàng > user (STOCK_CHANNEL_MAPPING)")
public class StockChannelMappingController {

    private final StockChannelMappingService mappingService;

    @Operation(operationId = "findActiveStockChannelMapping", summary = "Lấy toàn bộ mapping đang hiệu lực",
            description = "Trả về danh sách toàn bộ bản ghi STOCK_CHANNEL_MAPPING đang hiệu lực (status = 1).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = MAPPING_LIST_EXAMPLE)))
    })
    @GetMapping("/findActive")
    public StandardResponse<List<StockChannelMappingResponse>> findActive() {
        return StandardResponses.success(mappingService.findActive());
    }

    @Operation(operationId = "findStockChannelMappingByChannelType", summary = "Lấy danh sách mapping theo loại kênh",
            description = "Tra cứu các bản ghi STOCK_CHANNEL_MAPPING đang hiệu lực theo CHANNEL_TYPE_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = MAPPING_LIST_EXAMPLE)))
    })
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
