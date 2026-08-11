package com.viettel.bccs.policy.mapactiveinfo.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.RequestMbccs;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.GetProductCodeByMapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product-policy-service/v1/map-active-info")
@RequiredArgsConstructor
@Validated
@Tag(name = "Nghiệp vụ đấu nối di động trả trước", description = "API cho nghiệp vụ đấu nối di động trả trước")
public class MapActiveInfoProductController {
    private final MapActiveInfoProductService mapActiveInfoProductService;

    private static final String PRODUCT_CODE_LIST_EXAMPLE = """
            {
              "code": "SUCCESS",
              "message": "Thành công",
              "traceId": "5f2a3b1c-1234-4d5e-8a9b-000000000003",
              "requestId": "req-0003",
              "timestamp": "2026-08-11T02:00:00Z",
              "data": {
                "productOfferingDTOs": [
                  {
                    "code": "PACKAGE_001",
                    "name": "Gói cước data 50GB",
                    "productOfferingId": 12345,
                    "productOfferTypeId": 1,
                    "telecomServiceId": 1,
                    "status": "1"
                  }
                ]
              }
            }""";

    @PostMapping("/getProductCodeByMapActiveInfo")
    @Operation(operationId = "API_PRODUCT_023",
            summary = "API lấy danh sách gói cước",
            description = "API lấy danh sách gói cước theo map active info, loc theo vai tro nhan vien (M2M, goi dac biet, goi thuong)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_CODE_LIST_EXAMPLE)))
    })
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
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_CODE_LIST_EXAMPLE)))
    })
    public StandardResponse<GetProductCodeByMapActiveInfoResponse> getProductCodeNew(
            @Valid @RequestBody RequestMbccs request) {
        return StandardResponses.success(GetProductCodeByMapActiveInfoResponse.builder()
                .productOfferingDTOs(mapActiveInfoProductService.getProductCodeNew(request))
                .build());
    }
}