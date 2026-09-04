package com.viettel.bccs.organization.shop.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.shop.dto.request.GetListStockValidRequest;
import com.viettel.bccs.organization.shop.openapi.ApiGetListStockMbccs;
import com.viettel.bccs.organization.shop.openapi.ApiGetListStockValid;
import com.viettel.bccs.organization.shop.openapi.ApiValidateStockMapping;
import com.viettel.bccs.organization.shop.service.StockService;
import com.viettel.bccs.organization.staff.dto.StockDTO;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "VTT1", required = false)
            @PathVariable
            String staffCode,
            @Parameter(description = "ID dịch vụ viễn thông (TELECOM_SERVICE_ID)", example = "1")
            @RequestParam(required = false)
            Long telServiceId) {
        return StandardResponses.success(stockService.getListStockMbccs(staffCode, telServiceId));
    }

    @ApiValidateStockMapping
    @GetMapping("/validateStockMapping")
    public StandardResponse<Boolean> validateStockMapping(
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "NV_001", required = false)
            @RequestParam
            String staffCode,
            @Parameter(description = "Mã kho số cần kiểm tra", example = "GIUSO_TT", required = false)
            @RequestParam
            String stockCode,
            @Parameter(description = "ID dịch vụ viễn thông — cần thiết để kiểm tra kho chức năng", example = "1")
            @RequestParam(required = false)
            Long telServiceId) {
        return StandardResponses.success(stockService.validateStockMapping(staffCode, stockCode, telServiceId));
    }

    @ApiGetListStockValid
    @PostMapping("/getListStockValid")
    public StandardResponse<List<StockDTO>> getListStockValid(
            @RequestBody @Valid GetListStockValidRequest request) {
        return StandardResponses.success(stockService.getListStockValid(
                request.getStaffCode(), request.getShopIds(), request.getTelServiceId()));
    }

}
