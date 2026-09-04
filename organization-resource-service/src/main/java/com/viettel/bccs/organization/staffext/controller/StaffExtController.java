package com.viettel.bccs.organization.staffext.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.staffext.dto.response.StaffExtResponse;
import com.viettel.bccs.organization.staffext.openapi.ApiGetByStaffId;
import com.viettel.bccs.organization.staffext.openapi.ApiGetByStaffIdAndStatus;
import com.viettel.bccs.organization.staffext.openapi.ApiGetStaffExtByStaffIDAndKey;
import com.viettel.bccs.organization.staffext.service.StaffExtService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/organization-resource-service/v1/staffext")
@RequiredArgsConstructor
@Tag(name = "StaffExt", description = "Tra cứu thông tin mở rộng của nhân viên (STAFF_EXT)")
public class StaffExtController {

    private final StaffExtService staffExtService;

    @ApiGetByStaffId
    @GetMapping("/getByStaffId/{staffId}")
    public StandardResponse<List<StaffExtResponse>> getByStaffId(
            @Parameter(description = "ID nhân viên (STAFF_ID)", example = "12345", required = true)
            @PathVariable
            Long staffId) {
        return StandardResponses.success(staffExtService.getByStaffId(staffId));
    }

    @ApiGetByStaffIdAndStatus
    @GetMapping("/getByStaffIdAndStatus")
    public StandardResponse<List<StaffExtResponse>> getByStaffIdAndStatus(
            @Parameter(description = "ID nhân viên (STAFF_ID)", example = "12345", required = true)
            @RequestParam(required = false)
            Long staffId,
            @Parameter(description = "Trạng thái (STATUS)", example = "1", required = true)
            @RequestParam(required = false)
            String status) {
        return StandardResponses.success(staffExtService.getByStaffIdAndStatus(staffId, status));
    }

    @ApiGetStaffExtByStaffIDAndKey
    @GetMapping("/getStaffExtByStaffIDAndKey")
    public StandardResponse<StaffExtResponse> getStaffExtByStaffIDAndKey(
            @Parameter(description = "ID nhân viên (STAFF_ID)", example = "12345", required = true)
            @RequestParam(required = false)
            Long staffId,
            @Parameter(description = "Khoá thông tin mở rộng (KEY)", example = "AVATAR_URL", required = true)
            @RequestParam(required = false)
            String key) {
        StaffExtResponse response = staffExtService.getStaffExtByStaffIDAndKey(staffId, key);
        return StandardResponses.success(response);
    }

}
