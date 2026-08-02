package com.viettel.bccs.productcatalog.optionset.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetResponse;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-catalog-service/v1/optionset")
@RequiredArgsConstructor
public class OptionSetController {

    private final OptionSetService optionSetService;

    @GetMapping
    public StandardResponse<List<OptionSetResponse>> getAll() {
        return StandardResponses.success(optionSetService.getAll());
    }

    @GetMapping("/getById/{id}")
    public StandardResponse<OptionSetResponse> getById(@PathVariable Long id) {
        return StandardResponses.success(optionSetService.getById(id));
    }

    @GetMapping("/getByCode/{code}")
    public StandardResponse<OptionSetResponse> getByCode(@PathVariable String code) {
        return StandardResponses.success(optionSetService.getByCode(code));
    }

}