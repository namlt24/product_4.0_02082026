package com.viettel.bccs.policy.mapactiveinfo.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.GetProductCodeByMapActiveInfoRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.request.GetProductCodeRequest;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.GetProductCodeByMapActiveInfoResponse;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.viettel.bccs.policy.mapactiveinfo.openapi.MapActiveInfoProductControllerExamples.*;

@RestController
@RequestMapping("/product-policy-service/v1/map-active-info")
@RequiredArgsConstructor
@Tag(name = "Nghiệp vụ đấu nối di động trả trước", description = "API cho nghiệp vụ đấu nối di động trả trước")
public class MapActiveInfoProductController {
    private final MapActiveInfoProductService mapActiveInfoProductService;

    @PostMapping("/getProductCodeByMapActiveInfo")
    @Operation(operationId = "getProductCodeByMapActiveInfo",
            summary = "API lấy danh sách gói cước",
            description = "API lấy danh sách gói cước theo map active info, loc theo vai tro nhan vien (M2M, goi dac biet, goi thuong)",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = GetProductCodeByMapActiveInfoRequest.class),
                            examples = @ExampleObject(name = "request", value = PRODUCT_CODE_BY_MAP_ACTIVE_INFO_REQUEST_EXAMPLE))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_CODE_LIST_EXAMPLE)))
    })
    public StandardResponse<GetProductCodeByMapActiveInfoResponse> getProductCodeByMapActiveInfo(
            @RequestBody GetProductCodeByMapActiveInfoRequest request) {
        return StandardResponses.success(GetProductCodeByMapActiveInfoResponse.builder()
                .productOfferingDTOs(mapActiveInfoProductService.getProductCodeByMapActiveInfo(request))
                .build());
    }

    @PostMapping("/getProductCode")
    @Operation(operationId = "getProductCode",
            summary = "API lấy danh sách gói cước theo map active info",
            description = "Trả về danh sách gói cước (product code) hợp lệ theo map active info. Luôn ép kiểu PRODUCT_CODE và checkProductStatus=true, không lọc theo VAS.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(schema = @Schema(implementation = GetProductCodeRequest.class),
                            examples = @ExampleObject(name = "request", value = PRODUCT_CODE_REQUEST_EXAMPLE))))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = StandardResponse.class),
                            examples = @ExampleObject(name = "success", value = PRODUCT_CODE_LIST_EXAMPLE)))
    })
    public StandardResponse<GetProductCodeByMapActiveInfoResponse> getProductCode(
            @RequestBody GetProductCodeRequest request) {
        return StandardResponses.success(GetProductCodeByMapActiveInfoResponse.builder()
                .productOfferingDTOs(mapActiveInfoProductService.getProductCode(request))
                .build());
    }
}