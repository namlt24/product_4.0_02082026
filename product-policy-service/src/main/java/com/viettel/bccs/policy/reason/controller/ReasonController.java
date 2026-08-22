package com.viettel.bccs.policy.reason.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.RequestMbccs;
import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
import com.viettel.bccs.policy.reason.service.ReasonService;
import com.viettel.bccs.policy.utils.DataUtil;
import com.viettel.bccs.policy.utils.RequestValidator;
import com.viettel.bccs.policy.utils.ValidationPatterns;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.viettel.bccs.policy.reason.openapi.ReasonControllerExamples.*;

@RestController
@RequestMapping("/product-policy-service/v1/reason")
@RequiredArgsConstructor
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
            Long id) {
        RequestValidator.checkRange(id, "id", 0L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
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
            @RequestParam(required = false)
            Long reasonId,
            @Parameter(description = "Mã đặc tính (attribute code)", example = "COLOR", required = true)
            @RequestParam(required = false)
            String attributeCode) {
        RequestValidator.requireNotNull(reasonId, "reasonId", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkRange(reasonId, "reasonId", 0L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
        RequestValidator.requireNotBlank(attributeCode, "attributeCode", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(attributeCode, "attributeCode", 50, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(attributeCode, "attributeCode", ValidationPatterns.CODE, "BCCS-POLICY-VALIDATE-PATTERN");
        return StandardResponses.success(service.checkAttReason(reasonId, attributeCode));
    }

    @GetMapping("/getReasonCharacter")
    @Operation(operationId = "getReasonCharacter", summary = "Lấy danh sách mã thuộc tính của hình thức hòa mạng",
            description = "Trả về danh sách mã thuộc tính (product_spec_char.code) đang gán cho reason (reasonId), tra cứu qua REASON_CHAR_USE. Chỉ tính các bản ghi REASON_CHAR_USE đang active. Trả về danh sách rỗng nếu reason không có thuộc tính nào.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = REASON_CHARACTER_LIST_EXAMPLE)))
    })
    public StandardResponse<List<String>> getReasonCharacter(
            @Parameter(description = "ID hình thức hòa mạng", example = "1", required = true)
            @RequestParam(required = false)
            Long reasonId) {
        RequestValidator.requireNotNull(reasonId, "reasonId", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkRange(reasonId, "reasonId", 0L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
        return StandardResponses.success(service.getReasonCharacter(reasonId));
    }

    @GetMapping("/getReasonIdByTypeAndCode")
    @Operation(operationId = "getReasonIdByTypeAndCode", summary = "Tìm reasonId theo mã lý do, mã hành động và dịch vụ viễn thông",
            description = "Tra cứu REASON còn hiệu lực, khớp reason_code (regType), reason_type của actionCode và telecomServiceId (hoặc reason không ràng buộc dịch vụ). Trả về reasonId bản ghi đầu tiên (sắp theo name ASC), hoặc null nếu không tìm thấy. Phục vụ product-catalog-service (API getListStockTypeWS).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = REASON_ID_EXAMPLE)))
    })
    public StandardResponse<Long> getReasonIdByTypeAndCode(
            @Parameter(description = "Mã lý do (REASON_CODE)", example = "2", required = true)
            @RequestParam(required = false)
            String reasonCode,
            @Parameter(description = "Mã hành động (ACTION_CODE)", example = "00", required = true)
            @RequestParam(required = false)
            String actionCode,
            @Parameter(description = "ID dịch vụ viễn thông", example = "1", required = true)
            @RequestParam(required = false)
            Long telecomServiceId) {
        RequestValidator.requireNotBlank(reasonCode, "reasonCode", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(reasonCode, "reasonCode", 20, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(reasonCode, "reasonCode", ValidationPatterns.CODE, "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.requireNotBlank(actionCode, "actionCode", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(actionCode, "actionCode", 10, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(actionCode, "actionCode", ValidationPatterns.CODE, "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.requireNotNull(telecomServiceId, "telecomServiceId", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkRange(telecomServiceId, "telecomServiceId", 0L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
        return StandardResponses.success(service.getReasonIdByTypeAndCode(reasonCode, actionCode, telecomServiceId));
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
            @RequestParam(required = false)
            String actionCode,
            @Parameter(description = "ID dịch vụ viễn thông", example = "1", required = true)
            @RequestParam(required = false)
            Long telServiceId,
            @Parameter(description = "Hình thức thanh toán: 1 Trả sau, 2 Trả trước", example = "1", required = true)
            @RequestParam(required = false)
            String payType) {
        RequestValidator.requireNotBlank(actionCode, "actionCode", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(actionCode, "actionCode", 20, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(actionCode, "actionCode", ValidationPatterns.CODE, "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.requireNotNull(telServiceId, "telServiceId", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkRange(telServiceId, "telServiceId", 0L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
        RequestValidator.requireNotBlank(payType, "payType", "BCCS-POLICY-VALIDATE-REQUIRED");
        RequestValidator.checkMaxLength(payType, "payType", 1, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(payType, "payType", ValidationPatterns.PAY_TYPE_12, "BCCS-POLICY-VALIDATE-PATTERN");
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
