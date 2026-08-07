package com.viettel.bccs.productcatalog.productspeccharvalue.controller;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productspeccharvalue.dto.response.ProductSpecCharValueResponse;
import com.viettel.bccs.productcatalog.productspeccharvalue.service.ProductSpecCharValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product-catalog-service/v1/productspectcharvalue")
@RequiredArgsConstructor
public class ProductSpecCharValueController {

    private final ProductSpecCharValueService productSpecCharValueService;

    @PostMapping("/findByIds")
    public StandardResponse<List<ProductSpecCharValueResponse>> findByIds(@RequestBody List<Long> ids) {
        return StandardResponses.success(productSpecCharValueService.findByIds(ids));
    }

}