package com.viettel.bccs.productcatalog.productspeccharuse.controller;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.viettel.bccs.common.api.response.StandardResponse;
import com.viettel.bccs.common.api.response.StandardResponses;
import com.viettel.bccs.productcatalog.productspeccharuse.dto.response.ProductSpecCharUseResponse;
import com.viettel.bccs.productcatalog.productspeccharuse.openapi.ApiFindByIds;
import com.viettel.bccs.productcatalog.productspeccharuse.service.ProductSpecCharUseService;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Product Spec Char Use", description = "APIs quản lý bản ghi sử dụng đặc tính sản phẩm")
@RestController
@RequestMapping("/product-catalog-service/v1/productspectcharuse")
@RequiredArgsConstructor
public class ProductSpecCharUseController {

    private final ProductSpecCharUseService productSpecCharUseService;

    @ApiFindByIds
    @PostMapping("/findByIds")
    public StandardResponse<List<ProductSpecCharUseResponse>> findByIds(
            @Parameter(description = "Danh sách ID bản ghi sử dụng đặc tính sản phẩm", required = true)
            @RequestBody
            List<Long> ids) {
        return StandardResponses.success(productSpecCharUseService.findByIds(ids));
    }
}
