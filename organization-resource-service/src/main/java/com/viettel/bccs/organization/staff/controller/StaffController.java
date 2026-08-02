package com.viettel.bccs.organization.staff.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.custtype.dto.CustTypeDTO;
import com.viettel.bccs.organization.staff.dto.StockDTO;
import com.viettel.bccs.organization.staff.dto.StaffDTO;
import com.viettel.bccs.organization.staff.dto.response.StaffResponse;
import com.viettel.bccs.organization.staff.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/organization-resource-service/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping("/getActiveById/{staffId}")
    public StandardResponse<StaffDTO> getActiveById(@PathVariable Long staffId) {
        return StandardResponses.success(staffService.getActiveById(staffId));
    }

    @GetMapping("/findActiveByStaffCode/{staffCode}")
    public StandardResponse<StaffDTO> findActiveByStaffCode(@PathVariable String staffCode) {
        return StandardResponses.success(staffService.findActiveByStaffCode(staffCode));
    }

    @GetMapping("/getListStockByStaffCode/{staffCode}")
    public StandardResponse<List<StockDTO>> getListStockByStaffCode(@PathVariable String staffCode) {
        return StandardResponses.success(staffService.getListStockByStaffCode(staffCode));
    }

    @GetMapping("/getStaffShopFullInfo/{staffCode}")
    @Operation(operationId = "API_PRODUCT_003",
            summary = "API lấy full thông tin của user",
            description = "API lấy full thông tin của user")
    public StandardResponse<StaffResponse> getStaffShopFullInfo(@PathVariable String staffCode) {
        return StandardResponses.success(staffService.getStaffShopFullInfo(staffCode));
    }

    @GetMapping("/getMappingChannelCustTypeV2")
    @Operation(operationId = "API_DAUNOI_TT_PRODUCT_001",
            summary = "API lấy mapping kênh - loại khách hàng theo staffCode và groupType",
            description = "API lấy mapping kênh - loại khách hàng theo staffCode")
    public StandardResponse<List<CustTypeDTO>> getMappingChannelCustTypeV2(
            @RequestParam(required = false) String staffCode,
            @RequestParam(required = false) String groupType) {
        return StandardResponses.success(staffService.getMappingChannelCustTypeV2(staffCode, groupType));
    }
}