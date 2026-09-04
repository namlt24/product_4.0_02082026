package com.viettel.bccs.area.area.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.area.area.dto.response.AreaResponse;
import com.viettel.bccs.area.area.openapi.ApiGetAll;
import com.viettel.bccs.area.area.openapi.ApiGetByAreaCode;
import com.viettel.bccs.area.area.openapi.ApiGetByParentCode;
import com.viettel.bccs.area.area.openapi.ApiGetByProvince;
import com.viettel.bccs.area.area.service.AreaService;
import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-area-service/v1/area")
@RequiredArgsConstructor
@Tag(name = "Area", description = "Tra cứu địa bàn hành chính (tỉnh/quận/phường)")
public class AreaController {

    private final AreaService areaService;

    @ApiGetAll
    @GetMapping("/getAll")
    public StandardResponse<List<AreaResponse>> getAll() {
        return StandardResponses.success(areaService.getAll());
    }

    @ApiGetByAreaCode
    @GetMapping("/getByAreaCode/{areaCode}")
    public StandardResponse<AreaResponse> getByAreaCode(
            @Parameter(description = "Mã địa bàn (AREA_CODE)", example = "A076003005001", required = true)
            @PathVariable
            String areaCode) {
        return StandardResponses.success(areaService.getByAreaCode(areaCode));
    }

    @ApiGetByParentCode
    @GetMapping("/getByParentCode/{parentCode}")
    public StandardResponse<List<AreaResponse>> getByParentCode(
            @Parameter(description = "Mã địa bàn cha (PARENT_CODE)", example = "A076", required = true)
            @PathVariable
            String parentCode) {
        return StandardResponses.success(areaService.getByParentCode(parentCode));
    }

    @ApiGetByProvince
    @GetMapping("/getByProvince")
    public StandardResponse<List<AreaResponse>> getByProvince(
            @Parameter(description = "Mã tỉnh/thành (PROVINCE)", example = "A076", required = true)
            @RequestParam(required = false)
            String province) {
        return StandardResponses.success(areaService.getByProvince(province));
    }
}
