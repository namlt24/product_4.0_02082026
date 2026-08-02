package com.viettel.bccs.policy.mapactiveinfo.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.exception.LogicException;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.IsCheckMapActiveInfoRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.ValidateInputMapActiveInfoRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.ValidateInputMapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.ValidateMapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoValidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product-policy-service/v1/map-active-info")
@RequiredArgsConstructor
@Tag(name = "Nghiệp vụ đấu nối di động trả trước", description = "API validate cho nghiệp vụ đấu nối di động trả trước")
public class MapActiveInfoValidateController {
    private final MapActiveInfoValidateService service;

    @PostMapping("/validateInputMapActiveInfo")
    public StandardResponse<ValidateInputMapActiveInfoResponse> validateInputMapActiveInfo(
            @Valid @RequestBody ValidateInputMapActiveInfoRequest request) {
        return StandardResponses.success(service.validateInputMapActiveInfo(request));
    }

    @PostMapping("/validateMapActiveInfo")
    public StandardResponse<ValidateMapActiveInfoResponse> validateMapActiveInfo(
            @Valid @RequestBody ValidateInputMapActiveInfoRequest request) throws LogicException {
        try {
            service.validateMapActiveInfo(request);
        } catch (LogicException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return StandardResponses.success(new ValidateMapActiveInfoResponse(true));
    }

    @PostMapping("/validateWithoutMapActiveInfo")
    public StandardResponse<ValidateMapActiveInfoResponse> validateWithoutMapActiveInfo(
            @Valid @RequestBody ValidateInputMapActiveInfoRequest request) {
        service.validateWithOutMapActiveInfo(request);
        return StandardResponses.success(new ValidateMapActiveInfoResponse(true));
    }

    @Operation(summary = "Validate theo thông tin mapping mới - trả về MapActiveInfoDTO")
    @PostMapping("/validateFollowMapActiveInfoNew")
    public StandardResponse<MapActiveInfoDTO> validateFollowMapActiveInfoNew(
            @Valid @RequestBody ValidateInputMapActiveInfoRequest request) {
        MapActiveInfoDTO mapActiveInfoDTO = MapActiveInfoDTO.builder()
                .actionCode("537")
                .businessNo(";-1;")
                .captcharRequire((short) 1)
                .changeTechnology(false)
                .channelTypeId(-1L)
                .checkVasCode(false)
                .createUser("PRODUCT")
                .customerGroup("-1")
                .customerType("-1")
                .deleteEndDate(false)
                .districtCode("-1")
                .districtName("Tất cả")
                .effectDate(java.time.LocalDateTime.of(2025, 10, 25, 0, 0, 0))
                .isError(false)
                .fileAction(0)
                .hasValidateArea(true)
                .id(1006743977000L)
                .issueDatetime(java.time.LocalDateTime.of(2025, 10, 25, 0, 0, 0))
                .nodeCode("-1;")
                .offerId(400000607L)
                .payMonthly(false)
                .payType("1")
                .precinctCode("-1")
                .productCode("POBAS")
                .productName("POBAS")
                .promCode("KM014")
                .provinceCode("-1")
                .provinceName("Tất cả")
                .reasonName("Auto_DoiKM1")
                .regReasonId(9903991084L)
                .shopCode("-1")
                .shopId(-1L)
                .staffCode("-1")
                .stationCodes("-1;")
                .stationId(-1L)
                .status("1")
                .subGroup("-1")
                .subType("-1")
                .technology("-1")
                .telServiceId(1L)
                .warnMappingAll(false)
                .build();
        return StandardResponses.success(mapActiveInfoDTO);
    }

}
