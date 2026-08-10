package com.viettel.bccs.policy.mapactiveinfo.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.IsCheckMapActiveInfoRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.ValidateInputMapActiveInfoRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.ValidateInputMapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.ValidateMapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoValidateService;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.DataUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/product-policy-service/v1/map-active-info")
@RequiredArgsConstructor
@Tag(name = "Nghiệp vụ đấu nối di động trả trước", description = "API validate cho nghiệp vụ đấu nối di động trả trước")
public class MapActiveInfoValidateController {
    private final MapActiveInfoValidateService service;


    @Operation(summary = "Validate theo thông tin mapping mới - trả về MapActiveInfoDTO")
    @PostMapping("/validateFollowMapActiveInfoNew")
    public StandardResponse<MapActiveInfoDTO> validateFollowMapActiveInfoNew(
            @Valid @RequestBody ValidateInputMapActiveInfoRequest request) {
        MapActiveInfoDTO mapActiveInfoDTO = new MapActiveInfoDTO();

        List<String> lstBusinessNo = request.getLstBusinessNo();

        if (DataUtil.notNullOrEmpty(lstBusinessNo)) {
            List<String> trimmedBusinessNo = new ArrayList<>(lstBusinessNo.size());
            for (String item : lstBusinessNo) {
                if (item != null) {
                    String trimmed = item.trim();
                    if (trimmed.length() > 3500) {
                        throw new BusinessException("BCCS-POLICY-MAPACTIVE-0015");
                    }
                    trimmedBusinessNo.add(trimmed);
                }
            }
            lstBusinessNo = trimmedBusinessNo;
        } else {
            lstBusinessNo = Collections.singletonList(Const.DEFAULT_VALUE_MAP_SELECT_ALL);
        }

        List<MapActiveInfoDTO> mapActiveInfoDTOs = service.validateMapActiveInfo(request.getStaffCode(),
                request.getActionCode(),
                request.getOfferIds() == null ? new ArrayList<Long>() : request.getOfferIds(),
                request.getPromotionCode(),
                request.getRegReasonId(),
                request.getCaptchaAnswer(),
                request.getTelServiceId(),
                request.getNowDate(),
                request.isNeedCheckCaptcha(),
                request.getProvince(),
                request.getDistrict(),
                request.getPrecinct(),
                request.getCustomerGroup(),
                request.getCustomerType(),
                request.getSubType(),
                request.getSubGroup(),
                request.getStationCodes(),
                request.getPayType(),
                request.getTechnology(),
                request.getMode(), null, lstBusinessNo);
        if (!DataUtil.isNullOrEmpty(mapActiveInfoDTOs)) {
            mapActiveInfoDTO = mapActiveInfoDTOs.get(0);
        }
        return StandardResponses.success(mapActiveInfoDTO);
    }

}
