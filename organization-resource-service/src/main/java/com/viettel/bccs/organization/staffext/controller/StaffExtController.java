package com.viettel.bccs.organization.staffext.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.staffext.dto.response.StaffExtResponse;
import com.viettel.bccs.organization.staffext.service.StaffExtService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/organization-resource-service/v1/staffext")
@RequiredArgsConstructor
public class StaffExtController {

    private final StaffExtService staffExtService;

    @GetMapping("/getByStaffId/{staffId}")
    public StandardResponse<List<StaffExtResponse>> getByStaffId(@PathVariable Long staffId) {
        return StandardResponses.success(staffExtService.getByStaffId(staffId));
    }

    @GetMapping("/getByStaffIdAndStatus")
    public StandardResponse<List<StaffExtResponse>> getByStaffIdAndStatus(
            @RequestParam Long staffId,
            @RequestParam String status) {
        return StandardResponses.success(staffExtService.getByStaffIdAndStatus(staffId, status));
    }

    @GetMapping("/getStaffExtByStaffIDAndKey")
    public StandardResponse<StaffExtResponse> getStaffExtByStaffIDAndKey(
            @RequestParam Long staffId,
            @RequestParam String key) {
        StaffExtResponse response = staffExtService.getStaffExtByStaffIDAndKey(staffId, key);
        return StandardResponses.success(response);
    }

}