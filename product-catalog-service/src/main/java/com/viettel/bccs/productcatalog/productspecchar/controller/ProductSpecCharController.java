package com.viettel.bccs.productcatalog.productspecchar.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productspecchar.dto.response.ProductSpecCharResponse;
import com.viettel.bccs.productcatalog.productspecchar.openapi.ApiFindByIds;
import com.viettel.bccs.productcatalog.productspecchar.openapi.ApiGetAll;
import com.viettel.bccs.productcatalog.productspecchar.openapi.ApiGetByCode;
import com.viettel.bccs.productcatalog.productspecchar.openapi.ApiGetById;
import com.viettel.bccs.productcatalog.productspecchar.service.ProductSpecCharService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-catalog-service/v1/productspecchar")
@RequiredArgsConstructor
public class ProductSpecCharController {

    private final ProductSpecCharService productSpecCharService;

    @ApiGetAll
    @GetMapping("/getAll")
    public StandardResponse<List<ProductSpecCharResponse>> getAll() {
        return StandardResponses.success(productSpecCharService.getAll());
    }

    @ApiGetById
    @GetMapping("/getById/{id}")
    public StandardResponse<ProductSpecCharResponse> getById(
            @Parameter(description = "ID thuộc tính sản phẩm (PRODUCT_SPEC_CHAR_ID)", example = "1", required = true)
            @PathVariable
            Long id) {
        return StandardResponses.success(productSpecCharService.getById(id));
    }

    @ApiGetByCode
    @GetMapping("/getByCode/{code}")
    public StandardResponse<ProductSpecCharResponse> getByCode(
            @Parameter(description = "Mã thuộc tính sản phẩm (CODE)", example = "COLOR", required = true)
            @PathVariable
            String code) {
        return StandardResponses.success(productSpecCharService.getByCode(code));
    }

    @ApiFindByIds
    @PostMapping("/findByIds")
    public StandardResponse<List<ProductSpecCharResponse>> findByIds(
            @Parameter(description = "Danh sách ID thuộc tính sản phẩm", required = true)
            @RequestBody
            List<Long> ids) {
        return StandardResponses.success(productSpecCharService.findByIds(ids));
    }

}
