package com.viettel.bccs.policy.reason.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.RequestMbccs;
import com.viettel.bccs.policy.mapping.service.MappingService;
import com.viettel.bccs.policy.reason.dto.response.GetReasonFullResponse;
import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
import com.viettel.bccs.policy.reason.openapi.ApiCheckAttReason;
import com.viettel.bccs.policy.reason.openapi.ApiFindById;
import com.viettel.bccs.policy.reason.openapi.ApiGetListReasonByActionCodeAndTelServiceForAudit;
import com.viettel.bccs.policy.reason.openapi.ApiGetReasonCharacter;
import com.viettel.bccs.policy.reason.openapi.ApiGetReasonFull;
import com.viettel.bccs.policy.reason.openapi.ApiGetReasonIdByTypeAndCode;
import com.viettel.bccs.policy.reason.openapi.ApiGetValuesByReasonAndSpec;
import com.viettel.bccs.policy.reason.service.ReasonService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-policy-service/v1/reason")
@RequiredArgsConstructor
public class ReasonController {

    private final ReasonService service;
    private final MappingService mappingService;

    @GetMapping("/findById/{id}")
    @ApiFindById
    public StandardResponse<ReasonResponse> findById(
            @Parameter(description = "ID hình thức hòa mạng (REASON_ID)", example = "1", required = true)
            @PathVariable
            Long id) {
        return StandardResponses.success(service.findById(id));
    }

    @GetMapping("/checkAttReason")
    @ApiCheckAttReason
    public StandardResponse<Boolean> checkAttReason(
            @Parameter(description = "ID hình thức hòa mạng", example = "1", required = true)
            @RequestParam(required = false)
            Long reasonId,
            @Parameter(description = "Mã đặc tính (attribute code)", example = "COLOR", required = true)
            @RequestParam(required = false)
            String attributeCode) {
        return StandardResponses.success(service.checkAttReason(reasonId, attributeCode));
    }

    @GetMapping("/getReasonCharacter")
    @ApiGetReasonCharacter
    public StandardResponse<List<String>> getReasonCharacter(
            @Parameter(description = "ID hình thức hòa mạng", example = "1", required = true)
            @RequestParam(required = false)
            Long reasonId) {
        return StandardResponses.success(service.getReasonCharacter(reasonId));
    }

    @GetMapping("/getValuesByReasonAndSpec")
    @ApiGetValuesByReasonAndSpec
    public StandardResponse<List<String>> getValuesByReasonAndSpec(
            @Parameter(description = "ID hình thức hòa mạng", example = "1", required = true)
            @RequestParam(required = false)
            Long reasonId,
            @Parameter(description = "Mã đặc tính (product_spec_char.code)", example = "COLOR", required = true)
            @RequestParam(required = false)
            String specCode) {
        return StandardResponses.success(service.getValuesByReasonAndSpec(reasonId, specCode));
    }

    @GetMapping("/getReasonIdByTypeAndCode")
    @ApiGetReasonIdByTypeAndCode
    public StandardResponse<Long> getReasonIdByTypeAndCode(
            @Parameter(description = "Mã lý do (REASON_CODE)", example = "2", required = true)
            @RequestParam(required = false)
            String reasonCode,
            @Parameter(description = "Mã hành động (ActionCode)", example = "00", required = true)
            @RequestParam(required = false)
            String actionCode,
            @Parameter(description = "ID dịch vụ viễn thông", example = "1", required = true)
            @RequestParam(required = false)
            Long telecomServiceId) {
        return StandardResponses.success(service.getReasonIdByTypeAndCode(reasonCode, actionCode, telecomServiceId));
    }

    @GetMapping("/getListReasonByActionCodeAndTelServiceForAudit")
    @ApiGetListReasonByActionCodeAndTelServiceForAudit
    public StandardResponse<List<ReasonDTO>> getListReasonByActionCodeAndTelServiceForAudit(
            @Parameter(description = "Mã hành động (ActionCode)", example = "NEW", required = true)
            @RequestParam(required = false)
            String actionCode,
            @Parameter(description = "ID dịch vụ viễn thông", example = "1", required = true)
            @RequestParam(required = false)
            Long telServiceId,
            @Parameter(description = "Hình thức thanh toán: 1 Trả sau, 2 Trả trước", example = "1", required = true)
            @RequestParam(required = false)
            String payType) {
        return StandardResponses.success(service.getListReasonByActionCodeAndTelServiceForAudit(actionCode,
                telServiceId, payType));
    }

    @RequestMapping(value = "/getReasonFull", method = RequestMethod.POST)
    @ApiGetReasonFull
    public StandardResponse<List<GetReasonFullResponse>> getReasonFull(@RequestBody RequestMbccs requestMbccs) {
        List<ReasonDTO> reasonDTOList = service.getReasonFull(
                requestMbccs.getStaffCode(),
                requestMbccs.getPayType(),
                requestMbccs.getOfferId(),
                requestMbccs.getActionCode(),
                requestMbccs.getServiceType(),
                requestMbccs.getProvince(),
                requestMbccs.getDistrict(),
                requestMbccs.getPrecint(),
                requestMbccs.getCustomerGroup(),
                requestMbccs.getCustomerType(),
                requestMbccs.getSubType(),
                requestMbccs.getSubGroup(),
                requestMbccs.getStationCodes(),
                requestMbccs.getPromotionCode(),
                requestMbccs.getTechnology(),
                requestMbccs.getMode(),
                requestMbccs.isGetReasonCharUse(),
                requestMbccs.getRoleMap(),
                requestMbccs.getNodeCode(),
                requestMbccs.getSingleOrCombo(),
                requestMbccs.getListProductSpec(),
                requestMbccs.getLstBusinessNo()
        );

        List<String> reasonCodes = reasonDTOList.stream().map(ReasonDTO::getReasonCode).collect(Collectors.toList());
        Map<String, String> mapReasonDvbh = mappingService.getLstMapPackageByActionCodeAndReasonCodes(reasonCodes,
                requestMbccs.getActionCode());

        List<GetReasonFullResponse> responses = reasonDTOList.stream()
                .map(reasonDTO -> GetReasonFullResponse.builder()
                        .reasonId(reasonDTO.getReasonId())
                        .reasonCode(reasonDTO.getReasonCode())
                        .name(reasonDTO.getName())
                        .reasonType(reasonDTO.getReasonType())
                        .saleServiceCode(mapReasonDvbh.getOrDefault(reasonDTO.getReasonCode(),
                                reasonDTO.getSaleServiceCode()))
                        .build())
                .toList();

        return StandardResponses.success(responses);
    }
}
