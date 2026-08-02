package com.viettel.bccs.area.area.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.area.area.dto.response.AreaResponse;
import com.viettel.bccs.area.area.service.AreaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-area-service/v1/area")
@RequiredArgsConstructor
public class AreaController {

    private final AreaService areaService;

    @GetMapping("/getAll")
    public StandardResponse<List<AreaResponse>> getAll() {
        return StandardResponses.success(areaService.getAll());
    }

    @GetMapping("/getByAreaCode/{areaCode}")
    public StandardResponse<AreaResponse> getByAreaCode(@PathVariable String areaCode) {
        return StandardResponses.success(areaService.getByAreaCode(areaCode));
    }

    @GetMapping("/getByParentCode/{parentCode}")
    public StandardResponse<List<AreaResponse>> getByParentCode(@PathVariable String parentCode) {
        return StandardResponses.success(areaService.getByParentCode(parentCode));
    }

    @GetMapping("/getByProvince")
    public StandardResponse<List<AreaResponse>> getByProvince(@RequestParam String province) {
        return StandardResponses.success(areaService.getByProvince(province));
    }
}