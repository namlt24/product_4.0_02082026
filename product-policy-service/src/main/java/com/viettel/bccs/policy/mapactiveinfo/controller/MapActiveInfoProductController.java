package com.viettel.bccs.policy.mapactiveinfo.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.GetProductCodeByMapActiveInfoRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.GetProductCodeRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.GetProductCodeByMapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.openapi.ApiGetProductCode;
import com.viettel.bccs.policy.mapactiveinfo.openapi.ApiGetProductCodeByMapActiveInfo;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product-policy-service/v1/map-active-info")
@RequiredArgsConstructor
@Tag(name = "Nghiệp vụ đấu nối di động trả trước", description = "API cho nghiệp vụ đấu nối di động trả trước")
public class MapActiveInfoProductController {
    private final MapActiveInfoProductService mapActiveInfoProductService;

    @PostMapping("/getProductCodeByMapActiveInfo")
    @ApiGetProductCodeByMapActiveInfo
    public StandardResponse<GetProductCodeByMapActiveInfoResponse> getProductCodeByMapActiveInfo(
            @RequestBody GetProductCodeByMapActiveInfoRequest request) {
        return StandardResponses.success(GetProductCodeByMapActiveInfoResponse.builder()
                .productOfferingDTOs(mapActiveInfoProductService.getProductCodeByMapActiveInfo(request))
                .build());
    }

    @PostMapping("/getProductCode")
    @ApiGetProductCode
    public StandardResponse<GetProductCodeByMapActiveInfoResponse> getProductCode(
            @RequestBody GetProductCodeRequest request) {
        return StandardResponses.success(GetProductCodeByMapActiveInfoResponse.builder()
                .productOfferingDTOs(mapActiveInfoProductService.getProductCode(request))
                .build());
    }
}
