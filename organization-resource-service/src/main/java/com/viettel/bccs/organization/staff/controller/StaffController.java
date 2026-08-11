package com.viettel.bccs.organization.staff.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.organization.custtype.dto.CustTypeDTO;
import com.viettel.bccs.organization.staff.dto.StockDTO;
import com.viettel.bccs.organization.staff.dto.StaffDTO;
import com.viettel.bccs.organization.staff.dto.response.StaffResponse;
import com.viettel.bccs.organization.staff.service.StaffService;
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
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.viettel.bccs.organization.staff.openapi.StaffControllerExamples.*;

@RestController
@RequestMapping("/organization-resource-service/v1/staff")
@RequiredArgsConstructor
@Validated
@Tag(name = "Staff", description = "Tra cứu thông tin nhân viên/điểm bán (STAFF)")
public class StaffController {

    private final StaffService staffService;

    @Operation(operationId = "getActiveStaffById", summary = "Lấy nhân viên active theo ID",
            description = "Tra cứu 1 bản ghi STAFF có STATUS active theo STAFF_ID (khoá chính).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = STAFF_DTO_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên với id tương ứng")
    })
    @GetMapping("/getActiveById/{staffId}")
    public StandardResponse<StaffDTO> getActiveById(
            @Parameter(description = "ID nhân viên (STAFF_ID)", example = "12345", required = true)
            @PathVariable
            @Min(value = 0, message = "staffId phải >= 0")
            @Max(value = 9999999999L, message = "staffId vượt quá độ dài cột (precision 10)")
            Long staffId) {
        return StandardResponses.success(staffService.getActiveById(staffId));
    }

    @Operation(operationId = "findActiveStaffByStaffCode", summary = "Lấy nhân viên active theo mã",
            description = "Tra cứu 1 bản ghi STAFF có STATUS active theo STAFF_CODE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = STAFF_DTO_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên với mã tương ứng")
    })
    @GetMapping("/findActiveByStaffCode/{staffCode}")
    public StandardResponse<StaffDTO> findActiveByStaffCode(
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "NV_001", required = true)
            @PathVariable
            @Size(max = 40, message = "staffCode tối đa 40 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
            String staffCode) {
        return StandardResponses.success(staffService.findActiveByStaffCode(staffCode));
    }

    @Operation(operationId = "getListStockByStaffCode", summary = "Lấy danh sách kho theo mã nhân viên",
            description = "Tra cứu danh sách kho (STOCK) gắn với nhân viên theo STAFF_CODE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = STOCK_LIST_EXAMPLE)))
    })
    @GetMapping("/getListStockByStaffCode/{staffCode}")
    public StandardResponse<List<StockDTO>> getListStockByStaffCode(
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "NV_001", required = true)
            @PathVariable
            @Size(max = 40, message = "staffCode tối đa 40 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
            String staffCode) {
        return StandardResponses.success(staffService.getListStockByStaffCode(staffCode));
    }

    @Operation(operationId = "API_PRODUCT_003",
            summary = "API lấy full thông tin của user",
            description = "API lấy full thông tin của user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = STAFF_SHOP_FULL_INFO_EXAMPLE)))
    })
    @GetMapping("/getStaffShopFullInfo/{staffCode}")
    public StandardResponse<StaffResponse> getStaffShopFullInfo(
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "NV_001", required = true)
            @PathVariable
            @Size(max = 40, message = "staffCode tối đa 40 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
            String staffCode) {
        return StandardResponses.success(staffService.getStaffShopFullInfo(staffCode));
    }

    @Operation(operationId = "getStaffShopFullInfoByStaffId",
            summary = "API lấy full thông tin của user theo staffId",
            description = "Tra cứu full thông tin nhân viên + shop (STAFF join SHOP) theo STAFF_ID thay vì STAFF_CODE, dùng cho các luồng caller đã có sẵn staffId.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = STAFF_SHOP_FULL_INFO_BY_ID_EXAMPLE))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhân viên với id tương ứng")
    })
    @GetMapping("/getStaffShopFullInfoByStaffId/{staffId}")
    public StandardResponse<StaffResponse> getStaffShopFullInfoByStaffId(
            @Parameter(description = "ID nhân viên (STAFF_ID)", example = "12345", required = true)
            @PathVariable
            @Min(value = 0, message = "staffId phải >= 0")
            @Max(value = 9999999999L, message = "staffId vượt quá độ dài cột (precision 10)")
            Long staffId) {
        return StandardResponses.success(staffService.getStaffShopFullInfoByStaffId(staffId));
    }

    @Operation(operationId = "API_DAUNOI_TT_PRODUCT_001",
            summary = "API lấy mapping kênh - loại khách hàng theo staffCode và groupType",
            description = "API lấy mapping kênh - loại khách hàng theo staffCode")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = CUST_TYPE_LIST_EXAMPLE)))
    })
    @GetMapping("/getMappingChannelCustTypeV2")
    public StandardResponse<List<CustTypeDTO>> getMappingChannelCustTypeV2(
            @Parameter(description = "Mã nhân viên (STAFF_CODE)", example = "NV_001")
            @RequestParam(required = false)
            @Size(max = 40, message = "staffCode tối đa 40 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,40}$", message = "staffCode chỉ gồm chữ, số, '_' hoặc '-'")
            String staffCode,
            @Parameter(description = "Loại khách hàng: 1 Cá nhân trong nước, 2 Doanh nghiệp, 3 Nước ngoài", example = "1")
            @RequestParam(required = false)
            @Size(max = 1, message = "groupType tối đa 1 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{0,1}$", message = "groupType chỉ gồm chữ, số, '_' hoặc '-'")
            String groupType) {
        return StandardResponses.success(staffService.getMappingChannelCustTypeV2(staffCode, groupType));
    }
}
