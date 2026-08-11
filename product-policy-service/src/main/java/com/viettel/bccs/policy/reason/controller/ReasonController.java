package com.viettel.bccs.policy.reason.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.RequestMbccs;
import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
import com.viettel.bccs.policy.reason.service.ReasonService;
import com.viettel.bccs.policy.utils.DataUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.viettel.bccs.policy.reason.openapi.ReasonControllerExamples.*;

@RestController
@RequestMapping("/product-policy-service/v1/reason")
@RequiredArgsConstructor
@Validated
public class ReasonController {

    private final ReasonService service;

    @GetMapping("/findById/{id}")
    @Operation(operationId = "findReasonById", summary = "Lấy hình thức hòa mạng theo ID",
            description = "Tra cứu 1 bản ghi REASON theo REASON_ID (khoá chính).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = REASON_SINGLE_EXAMPLE)))
    })
    public StandardResponse<ReasonResponse> findById(
            @Parameter(description = "ID hình thức hòa mạng (REASON_ID)", example = "1", required = true)
            @PathVariable
            @Min(value = 0, message = "id phải >= 0")
            @Max(value = 9999999999L, message = "id vượt quá độ dài cột (precision 10)")
            Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @GetMapping("/checkAttReason")
    @Operation(operationId = "checkAttReason", summary = "Kiểm tra hình thức hòa mạng (reason) có đặc tính theo mã",
            description = "Kiểm tra reason (reasonId) có gán đặc tính (product_spec_char, tra cứu qua REASON_CHAR_USE) theo attributeCode hay không. Chỉ tính các bản ghi REASON_CHAR_USE đang active.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = CHECK_ATT_REASON_EXAMPLE)))
    })
    public StandardResponse<Boolean> checkAttReason(
            @Parameter(description = "ID hình thức hòa mạng", example = "1", required = true)
            @RequestParam
            @Min(value = 0, message = "reasonId phải >= 0")
            @Max(value = 9999999999L, message = "reasonId vượt quá độ dài cột (precision 10)")
            Long reasonId,
            @Parameter(description = "Mã đặc tính (attribute code)", example = "COLOR", required = true)
            @RequestParam
            @Size(min = 1, max = 50, message = "attributeCode tối đa 50 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,50}$", message = "attributeCode chỉ gồm chữ, số, '_' hoặc '-'")
            String attributeCode) {
        return StandardResponses.success(service.checkAttReason(reasonId, attributeCode));
    }

    @GetMapping("/getListReasonByActionCodeAndTelServiceForAudit")
    @Operation(operationId = "getListReasonByActionCodeAndTelServiceForAudit",
            summary = "Lấy danh sách hình thức hòa mạng theo mã hành động, dịch vụ viễn thông",
            description = "Tra cứu danh sách REASON phục vụ audit theo actionCode, telServiceId, payType.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = REASON_DTO_LIST_EXAMPLE)))
    })
    public StandardResponse<List<ReasonDTO>> getListReasonByActionCodeAndTelServiceForAudit(
            @Parameter(description = "Mã hành động (ACTION_CODE)", example = "NEW", required = true)
            @RequestParam
            @Size(min = 1, max = 20, message = "actionCode tối đa 20 ký tự")
            @Pattern(regexp = "^[A-Za-z0-9_-]{1,20}$", message = "actionCode chỉ gồm chữ, số, '_' hoặc '-'")
            String actionCode,
            @Parameter(description = "ID dịch vụ viễn thông", example = "1", required = true)
            @RequestParam
            @Min(value = 0, message = "telServiceId phải >= 0")
            @Max(value = 9999999999L, message = "telServiceId vượt quá độ dài cột (precision 10)")
            Long telServiceId,
            @Parameter(description = "Hình thức thanh toán: 1 Trả sau, 2 Trả trước", example = "1", required = true)
            @RequestParam
            @Size(min = 1, max = 1, message = "payType đúng 1 ký tự")
            @Pattern(regexp = "^[12]$", message = "payType chỉ nhận giá trị 1 hoặc 2")
            String payType) {
        return StandardResponses.success(service.getListReasonByActionCodeAndTelServiceForAudit(actionCode, telServiceId, payType));
    }

    @RequestMapping(value = "/getReasonFull", method = RequestMethod.POST)
    @Operation(operationId = "API_PRODUCT_002",
            summary = "API lấy danh sách hình thức hòa mạng",
            description = "API lấy danh sách hình thức hòa mạng")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = REASON_DTO_LIST_EXAMPLE)))
    })
    public StandardResponse<List<ReasonDTO>> getReasonFull(@RequestBody RequestMbccs requestMDealer) {
            List<ReasonDTO> reasonDTOList = service.getReasonFull(
                    requestMDealer.getStaffCode(),
                    requestMDealer.getPayType(),
                    requestMDealer.getOfferId(),
                    requestMDealer.getActionCode(),
                    requestMDealer.getServiceType(),
                    requestMDealer.getProvince(),
                    requestMDealer.getDistrict(),
                    requestMDealer.getPrecint(),
                    requestMDealer.getCustomerGroup(),
                    requestMDealer.getCustomerType(),
                    requestMDealer.getSubType(),
                    requestMDealer.getSubGroup(),
                    requestMDealer.getStationCodes(),
                    requestMDealer.getPromotionCode(),
                    requestMDealer.getTechnology(),
                    requestMDealer.getMode(),
                    requestMDealer.isGetReasonCharUse(),
                    requestMDealer.getRoleMap(),
                    requestMDealer.getNodeCode(),
                    requestMDealer.getSingleOrCombo(),
                    requestMDealer.getListProductSpec(),
                    requestMDealer.getLstBusinessNo()
            );
//            if (!DataUtil.isNullOrEmpty(reasonDTOList)) {
//                List<String> reasonCodes = reasonDTOList.stream().map(x -> x.getReasonCode()).collect(Collectors.toList());
//                Map<String, String> mapReasonDVBH = productPackageService.getLstMapPackageByActionCodeAndReasonCodes(reasonCodes, requestMDealer.getActionCode());
//                for (ReasonDTO reasonDTO : reasonDTOList) {
//                    String currentReasonCode = reasonDTO.getReasonCode();
//
//                    if (mapReasonDVBH.containsKey(currentReasonCode)) {
//                        String saleServiceCode = mapReasonDVBH.get(currentReasonCode);
//
//                        reasonDTO.setSaleServiceCode(saleServiceCode);
//                    }
//                }
//            }

        return StandardResponses.success(reasonDTOList);
    }
}
