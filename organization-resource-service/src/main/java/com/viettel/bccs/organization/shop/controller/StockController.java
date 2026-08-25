package com.viettel.bccs.organization.shop.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.shop.openapi.ApiGetListStockMbccs;
import com.viettel.bccs.organization.shop.openapi.ApiValidateStockMapping;
import com.viettel.bccs.organization.shop.service.StockService;
import com.viettel.bccs.organization.staff.dto.StockDTO;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization-resource-service/v1/stock")
@RequiredArgsConstructor
@Validated
@Tag(name = "Stock", description = "Tra cứu thông tin kho số (STOCK)")
public class StockController {

    private final StockService stockService;

    @ApiGetListStockMbccs
    @GetMapping("/getListStockMbccs/{staffCode}")
    public StandardResponse<List<StockDTO>> getListStockMbccs(
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "NV_001", required = true)
            @PathVariable
            @Size(max = 40, message = "staffCode tối đa 40 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
            String staffCode,
            @Parameter(description = "ID dịch vụ viễn thông (TELECOM_SERVICE_ID)", example = "1")
            @RequestParam(required = false)
            @Min(value = 0, message = "telServiceId phải >= 0")
            @Max(value = 9999999999L, message = "telServiceId vượt quá độ dài cột (precision 10)")
            Long telServiceId) {
        return StandardResponses.success(stockService.getListStockMbccs(staffCode, telServiceId));
    }

    @ApiValidateStockMapping
    @GetMapping("/validateStockMapping")
    public StandardResponse<Boolean> validateStockMapping(
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "NV_001", required = true)
            @RequestParam
            @NotBlank(message = "staffCode không được để trống")
            @Size(max = 40, message = "staffCode tối đa 40 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,40}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
            String staffCode,
            @Parameter(description = "Mã kho số cần kiểm tra", example = "GIUSO_TT", required = true)
            @RequestParam
            @NotBlank(message = "stockCode không được để trống")
            @Size(max = 40, message = "stockCode tối đa 40 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,40}$", message = "stockCode chỉ gồm chữ, số, '_' hoặc '-'")
            String stockCode,
            @Parameter(description = "ID dịch vụ viễn thông — cần thiết để kiểm tra kho chức năng", example = "1")
            @RequestParam(required = false)
            @Min(value = 0, message = "telServiceId phải >= 0")
            @Max(value = 9999999999L, message = "telServiceId vượt quá độ dài cột (precision 10)")
            Long telServiceId) {
        return StandardResponses.success(stockService.validateStockMapping(staffCode, stockCode, telServiceId));
    }

}
