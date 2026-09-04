package com.viettel.bccs.productcatalog.productspeccharvalue.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productspeccharvalue.dto.response.ProductSpecCharValueResponse;
import com.viettel.bccs.productcatalog.productspeccharvalue.openapi.ApiFindByIds;
import com.viettel.bccs.productcatalog.productspeccharvalue.service.ProductSpecCharValueService;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/product-catalog-service/v1/productspectcharvalue")
@RequiredArgsConstructor
public class ProductSpecCharValueController {

    private final ProductSpecCharValueService productSpecCharValueService;

    @ApiFindByIds
    @PostMapping("/findByIds")
    public StandardResponse<List<ProductSpecCharValueResponse>> findByIds(
            @Parameter(description = "Danh sách ID giá trị thuộc tính sản phẩm", required = true)
            @RequestBody
            List<Long> ids) {
        return StandardResponses.success(productSpecCharValueService.findByIds(ids));
    }

}
