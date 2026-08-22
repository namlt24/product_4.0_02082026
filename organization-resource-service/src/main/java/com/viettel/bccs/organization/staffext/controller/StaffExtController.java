package com.viettel.bccs.organization.staffext.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.staffext.dto.response.StaffExtResponse;
import com.viettel.bccs.organization.staffext.service.StaffExtService;
import com.viettel.bccs.organization.utils.RequestValidator;
import com.viettel.bccs.organization.utils.ValidationPatterns;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.viettel.bccs.organization.staffext.openapi.StaffExtControllerExamples.*;

@RestController
@RequestMapping("/organization-resource-service/v1/staffext")
@RequiredArgsConstructor
@Tag(name = "StaffExt", description = "Tra cứu thông tin mở rộng của nhân viên (STAFF_EXT)")
public class StaffExtController {

    private final StaffExtService staffExtService;

    @Operation(operationId = "getStaffExtByStaffId", summary = "Lấy danh sách thông tin mở rộng theo staffId",
            description = "Tra cứu toàn bộ bản ghi STAFF_EXT theo STAFF_ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = STAFF_EXT_LIST_EXAMPLE)))
    })
    @GetMapping("/getByStaffId/{staffId}")
    public StandardResponse<List<StaffExtResponse>> getByStaffId(
            @Parameter(description = "ID nhân viên (STAFF_ID)", example = "12345", required = true)
            @PathVariable
            Long staffId) {
        RequestValidator.checkRange(staffId, "staffId", 0L, 9999999999L, "BCCS-ORGANIZATION-VALIDATE-RANGE");
        return StandardResponses.success(staffExtService.getByStaffId(staffId));
    }

    @Operation(operationId = "getStaffExtByStaffIdAndStatus", summary = "Lấy danh sách thông tin mở rộng theo staffId và status",
            description = "Tra cứu các bản ghi STAFF_EXT theo STAFF_ID và STATUS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = STAFF_EXT_LIST_EXAMPLE)))
    })
    @GetMapping("/getByStaffIdAndStatus")
    public StandardResponse<List<StaffExtResponse>> getByStaffIdAndStatus(
            @Parameter(description = "ID nhân viên (STAFF_ID)", example = "12345", required = true)
            @RequestParam(required = false)
            Long staffId,
            @Parameter(description = "Trạng thái (STATUS)", example = "1", required = true)
            @RequestParam(required = false)
            String status) {
        RequestValidator.checkRange(staffId, "staffId", 0L, 9999999999L, "BCCS-ORGANIZATION-VALIDATE-RANGE");
        RequestValidator.requireNotBlank(status, "status", "BCCS-ORGANIZATION-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(status, "status", 2, "BCCS-ORGANIZATION-VALIDATE-SIZE");
        RequestValidator.checkPattern(status, "status", ValidationPatterns.ALPHANUMERIC, "BCCS-ORGANIZATION-VALIDATE-PATTERN");
        return StandardResponses.success(staffExtService.getByStaffIdAndStatus(staffId, status));
    }

    @Operation(operationId = "getStaffExtByStaffIdAndKey", summary = "Lấy thông tin mở rộng theo staffId và key",
            description = "Tra cứu 1 bản ghi STAFF_EXT theo STAFF_ID và KEY.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = STAFF_EXT_SINGLE_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thông tin mở rộng với staffId/key tương ứng")
    })
    @GetMapping("/getStaffExtByStaffIDAndKey")
    public StandardResponse<StaffExtResponse> getStaffExtByStaffIDAndKey(
            @Parameter(description = "ID nhân viên (STAFF_ID)", example = "12345", required = true)
            @RequestParam(required = false)
            Long staffId,
            @Parameter(description = "Khoá thông tin mở rộng (KEY)", example = "AVATAR_URL", required = true)
            @RequestParam(required = false)
            String key) {
        RequestValidator.checkRange(staffId, "staffId", 0L, 9999999999L, "BCCS-ORGANIZATION-VALIDATE-RANGE");
        RequestValidator.requireNotBlank(key, "key", "BCCS-ORGANIZATION-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(key, "key", 50, "BCCS-ORGANIZATION-VALIDATE-SIZE");
        RequestValidator.checkPattern(key, "key", ValidationPatterns.CODE, "BCCS-ORGANIZATION-VALIDATE-PATTERN");
        StaffExtResponse response = staffExtService.getStaffExtByStaffIDAndKey(staffId, key);
        return StandardResponses.success(response);
    }

}
