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

@RestController
@RequestMapping("/organization-resource-service/v1/staff")
@RequiredArgsConstructor
@Validated
@Tag(name = "Staff", description = "Tra cứu thông tin nhân viên/điểm bán (STAFF)")
public class StaffController {

    private final StaffService staffService;

    private static final String STAFF_DTO_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000021",
              "requestId": "req-0021",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "staffId": 12345,
                "staffCode": "NV_001",
                "name": "Nguyễn Văn A",
                "tel": "0909123456",
                "email": "nguyenvana@viettel.vn",
                "idNo": "001234567890",
                "status": "1",
                "statusName": "Dang hoat dong",
                "shopId": 12345,
                "shopCode": "VTST_HN_001",
                "channelTypeId": 1,
                "type": 1,
                "province": "HN",
                "district": "BD",
                "precinct": "P1",
                "areaCode": "HN"
              }
            }""";

    private static final String STOCK_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000022",
              "requestId": "req-0022",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "stockId": 1,
                  "code": "STOCK_001",
                  "name": "Kho hàng hóa Hà Nội",
                  "type": "1"
                }
              ]
            }""";

    private static final String STAFF_SHOP_FULL_INFO_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000023",
              "requestId": "req-0023",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "staffId": 12345,
                "staffCode": "NV_001",
                "name": "Nguyễn Văn A",
                "tel": "0909123456",
                "email": "nguyenvana@viettel.vn",
                "idNo": "001234567890",
                "idIssueDate": "2020-01-01",
                "idIssuePlace": "Hà Nội",
                "birthday": "1990-01-01",
                "address": "123 Nguyễn Trãi, Quận 1, TP HCM",
                "province": "HN",
                "district": "BD",
                "precinct": "P1",
                "shopId": 12345,
                "shopCode": "VTST_HN_001",
                "shopName": "Viettel Store Hà Nội",
                "status": "1",
                "statusName": "Dang hoat dong",
                "channelTypeId": 1,
                "channelTypeName": "Đại lý",
                "type": 1,
                "userId": 12345,
                "staffOwnerId": 12345,
                "staffOwnType": "OWNER",
                "areaCode": "HN",
                "shop": {
                  "shopId": 12345,
                  "name": "Viettel Store Hà Nội",
                  "shopCode": "VTST_HN_001",
                  "parentShopId": 10000,
                  "address": "123 Nguyễn Trãi, Quận 1, TP HCM",
                  "tel": "0909123456",
                  "email": "contact@viettel.vn",
                  "province": "HN",
                  "district": "BD",
                  "precinct": "P1",
                  "channelTypeId": 1,
                  "status": "1",
                  "shopPath": "/HN/VTST_HN_001",
                  "shopType": "1",
                  "areaCode": "HN",
                  "createDatetime": "2024-01-01T00:00:00.000+00:00",
                  "groupChannelTypeId": 5
                }
              }
            }""";

    private static final String CUST_TYPE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000024",
              "requestId": "req-0024",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": [
                {
                  "custType": "PREPAID",
                  "name": "Trả trước",
                  "createUser": "admin",
                  "createDatetime": "2024-01-01T00:00:00.000+00:00",
                  "updateUser": "admin",
                  "updateDatetime": "2024-06-01T00:00:00.000+00:00",
                  "description": "Khách hàng trả trước",
                  "status": "1",
                  "groupType": "1",
                  "tax": 10,
                  "plan": "1",
                  "representCust": "0",
                  "custTypeId": 1
                }
              ]
            }""";

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
