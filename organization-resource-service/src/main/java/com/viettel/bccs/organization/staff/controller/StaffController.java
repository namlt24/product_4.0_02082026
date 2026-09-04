package com.viettel.bccs.organization.staff.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.custtype.dto.CustTypeDTO;
import com.viettel.bccs.organization.staff.dto.StaffDTO;
import com.viettel.bccs.organization.staff.dto.StockDTO;
import com.viettel.bccs.organization.staff.dto.response.StaffResponse;
import com.viettel.bccs.organization.staff.dto.response.StaffSummaryDTO;
import com.viettel.bccs.organization.staff.openapi.ApiFindActiveByStaffCode;
import com.viettel.bccs.organization.staff.openapi.ApiFindActiveByStaffCodeWithChannelOfSalePoint;
import com.viettel.bccs.organization.staff.openapi.ApiGetActiveById;
import com.viettel.bccs.organization.staff.openapi.ApiGetApproveStaffOrder;
import com.viettel.bccs.organization.staff.openapi.ApiGetListStockByStaffCode;
import com.viettel.bccs.organization.staff.openapi.ApiGetMappingChannelCustTypeV2;
import com.viettel.bccs.organization.staff.openapi.ApiGetStaffShopFullInfo;
import com.viettel.bccs.organization.staff.openapi.ApiGetStaffShopFullInfoByStaffId;
import com.viettel.bccs.organization.staff.service.StaffService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/organization-resource-service/v1/staff")
@RequiredArgsConstructor
@Tag(name = "Staff", description = "Tra cứu thông tin nhân viên/điểm bán (STAFF)")
public class StaffController {

    private final StaffService staffService;

    @ApiGetActiveById
    @GetMapping("/getActiveById/{staffId}")
    public StandardResponse<StaffDTO> getActiveById(
            @Parameter(description = "ID nhân viên (STAFF_ID)", example = "12345", required = true)
            @PathVariable
            Long staffId) {
        return StandardResponses.success(staffService.getActiveById(staffId));
    }

    @ApiFindActiveByStaffCode
    @GetMapping("/findActiveByStaffCode/{staffCode}")
    public StandardResponse<StaffDTO> findActiveByStaffCode(
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "NV_001", required = true)
            @PathVariable
            String staffCode) {
        return StandardResponses.success(staffService.findActiveByStaffCode(staffCode));
    }

    @ApiFindActiveByStaffCodeWithChannelOfSalePoint
    @GetMapping("/findActiveByStaffCodeWithChannelOfSalePoint/{staffCode}")
    public StandardResponse<StaffDTO> findActiveByStaffCodeWithChannelOfSalePoint(
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "NV_001", required = true)
            @PathVariable
            String staffCode) {
        return StandardResponses.success(staffService.findActiveByStaffCodeWithChannelOfSalePoint(staffCode));
    }

    @ApiGetListStockByStaffCode
    @GetMapping("/getListStockByStaffCode/{staffCode}")
    public StandardResponse<List<StockDTO>> getListStockByStaffCode(
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "NV_001", required = true)
            @PathVariable
            String staffCode) {
        return StandardResponses.success(staffService.getListStockByStaffCode(staffCode));
    }

    @ApiGetStaffShopFullInfo
    @GetMapping("/getStaffShopFullInfo/{staffCode}")
    public StandardResponse<StaffResponse> getStaffShopFullInfo(
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "NV_001", required = true)
            @PathVariable
            String staffCode) {
        return StandardResponses.success(staffService.getStaffShopFullInfo(staffCode));
    }

    @ApiGetStaffShopFullInfoByStaffId
    @GetMapping("/getStaffShopFullInfoByStaffId/{staffId}")
    public StandardResponse<StaffResponse> getStaffShopFullInfoByStaffId(
            @Parameter(description = "ID nhân viên (STAFF_ID)", example = "12345", required = true)
            @PathVariable
            Long staffId) {
        return StandardResponses.success(staffService.getStaffShopFullInfoByStaffId(staffId));
    }

    @ApiGetMappingChannelCustTypeV2
    @GetMapping("/getMappingChannelCustTypeV2")
    public StandardResponse<List<CustTypeDTO>> getMappingChannelCustTypeV2(
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "NV_001")
            @RequestParam(required = false)
            String staffCode,
            @Parameter(description = "Loại khách hàng: 1 Cá nhân trong nước, 2 Doanh nghiệp, 3 Nước ngoài",
                    example = "1")
            @RequestParam(required = false)
            String groupType) {
        return StandardResponses.success(staffService.getMappingChannelCustTypeV2(staffCode, groupType));
    }

    @ApiGetApproveStaffOrder
    @GetMapping("/getApproveStaffOrder/{staffCode}")
    public StandardResponse<StaffSummaryDTO> getApproveStaffOrder(
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "NV_001", required = true)
            @PathVariable
            String staffCode) {
        return StandardResponses.success(staffService.getApproveStaffOrder(staffCode));
    }
}
