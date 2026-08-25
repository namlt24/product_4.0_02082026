package com.viettel.bccs.productcatalog.optionset.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetResponse;
import com.viettel.bccs.productcatalog.optionset.openapi.ApiGetAll;
import com.viettel.bccs.productcatalog.optionset.openapi.ApiGetByCode;
import com.viettel.bccs.productcatalog.optionset.openapi.ApiGetById;
import com.viettel.bccs.productcatalog.optionset.service.OptionSetService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-catalog-service/v1/optionset")
@RequiredArgsConstructor
@Tag(name = "OptionSet", description = "Tra cứu nhóm option set (danh mục dùng chung)")
public class OptionSetController {

    private final OptionSetService optionSetService;

    @ApiGetAll
    @GetMapping
    public StandardResponse<List<OptionSetResponse>> getAll() {
        return StandardResponses.success(optionSetService.getAll());
    }

    @ApiGetById
    @GetMapping("/getById/{id}")
    public StandardResponse<OptionSetResponse> getById(
            @Parameter(description = "Id nhóm option set (OPTION_SET_ID)", example = "1", required = true)
            @PathVariable
            Long id) {
        return StandardResponses.success(optionSetService.getById(id));
    }

    @ApiGetByCode
    @GetMapping("/getByCode/{code}")
    public StandardResponse<OptionSetResponse> getByCode(
            @Parameter(description = "Mã nhóm option set (CODE)", example = "CUST_TYPE_GROUP_TYPE", required = true)
            @PathVariable
            String code) {
        return StandardResponses.success(optionSetService.getByCode(code));
    }

}
