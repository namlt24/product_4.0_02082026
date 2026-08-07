package com.viettel.bccs.policy.reason.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.RequestMbccs;
import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
import com.viettel.bccs.policy.reason.service.ReasonService;
import com.viettel.bccs.policy.utils.DataUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product-policy-service/v1/reason")
@RequiredArgsConstructor
public class ReasonController {

    private final ReasonService service;

    @GetMapping("/findById/{id}")
    public StandardResponse<ReasonResponse> findById(@PathVariable Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @GetMapping("/checkAttReason")
    @Operation(summary = "Kiểm tra hình thức hòa mạng (reason) có đặc tính theo mã",
            description = "Kiểm tra reason (reasonId) có gán đặc tính (product_spec_char, tra cứu qua REASON_CHAR_USE) theo attributeCode hay không. Chỉ tính các bản ghi REASON_CHAR_USE đang active.")
    public StandardResponse<Boolean> checkAttReason(
            @RequestParam Long reasonId,
            @RequestParam String attributeCode) {
        return StandardResponses.success(service.checkAttReason(reasonId, attributeCode));
    }

    @GetMapping("/getListReasonByActionCodeAndTelServiceForAudit")
    public StandardResponse<List<ReasonDTO>> getListReasonByActionCodeAndTelServiceForAudit(
            @RequestParam String actionCode,
            @RequestParam Long telServiceId,
            @RequestParam String payType) {
        return StandardResponses.success(service.getListReasonByActionCodeAndTelServiceForAudit(actionCode, telServiceId, payType));
    }

    @RequestMapping(value = "/getReasonFull", method = RequestMethod.POST)
    @Operation(operationId = "API_PRODUCT_002",
            summary = "API lấy danh sách hình thức hòa mạng",
            description = "API lấy danh sách hình thức hòa mạng")
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