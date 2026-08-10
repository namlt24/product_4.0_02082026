package com.viettel.bccs.policy.mapactiveinfo.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.RequestMbccs;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.GetProductCodeByMapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    @Operation(operationId = "API_PRODUCT_023",
            summary = "API lấy danh sách gói cước",
            description = "API lấy danh sách gói cước theo map active info, loc theo vai tro nhan vien (M2M, goi dac biet, goi thuong)")
    public StandardResponse<GetProductCodeByMapActiveInfoResponse> getProductCodeByMapActiveInfo(
            @Valid @RequestBody RequestMbccs request) {
        return StandardResponses.success(GetProductCodeByMapActiveInfoResponse.builder()
                .productOfferingDTOs(mapActiveInfoProductService.getProductCodeByMapActiveInfo(request))
                .build());
    }

    @PostMapping("/getProductCodeNew")
    @Operation(operationId = "getProductCodeNew",
            summary = "API lấy danh sách gói cước theo map active info (bản mới)",
            description = "Trả về danh sách gói cước (product code) hợp lệ theo map active info. Luôn ép kiểu PRODUCT_CODE và checkProductStatus=true, không lọc theo VAS.")
    public StandardResponse<GetProductCodeByMapActiveInfoResponse> getProductCodeNew(
            @Valid @RequestBody RequestMbccs request) {
        return StandardResponses.success(GetProductCodeByMapActiveInfoResponse.builder()
                .productOfferingDTOs(mapActiveInfoProductService.getProductCodeNew(request))
                .build());
    }
}