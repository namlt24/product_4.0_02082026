package com.viettel.bccs.productcatalog.productspeccharvalue.service;

import com.viettel.bccs.productcatalog.productspeccharvalue.mapper.ProductSpecCharValueMapper;
import com.viettel.bccs.productcatalog.productspeccharvalue.repository.ProductSpecCharValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductSpecCharValueService {

    private final ProductSpecCharValueRepository productSpecCharValueRepository;
    private final ProductSpecCharValueMapper productSpecCharValueMapper;

}