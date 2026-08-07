package com.viettel.bccs.productcatalog.productspecchar.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productspecchar.dto.response.ProductSpecCharResponse;
import com.viettel.bccs.productcatalog.productspecchar.service.ProductSpecCharService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-catalog-service/v1/productspecchar")
@RequiredArgsConstructor
public class ProductSpecCharController {

    private final ProductSpecCharService productSpecCharService;

    @GetMapping("/getAll")
    public StandardResponse<List<ProductSpecCharResponse>> getAll() {
        return StandardResponses.success(productSpecCharService.getAll());
    }

    @GetMapping("/getById/{id}")
    public StandardResponse<ProductSpecCharResponse> getById(@PathVariable Long id) {
        return StandardResponses.success(productSpecCharService.getById(id));
    }

    @GetMapping("/getByCode/{code}")
    public StandardResponse<ProductSpecCharResponse> getByCode(@PathVariable String code) {
        return StandardResponses.success(productSpecCharService.getByCode(code));
    }

    @PostMapping("/findByIds")
    public StandardResponse<List<ProductSpecCharResponse>> findByIds(@RequestBody List<Long> ids) {
        return StandardResponses.success(productSpecCharService.findByIds(ids));
    }

}