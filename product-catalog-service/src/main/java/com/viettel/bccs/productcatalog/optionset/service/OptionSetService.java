package com.viettel.bccs.productcatalog.optionset.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetResponse;
import com.viettel.bccs.productcatalog.optionset.mapper.OptionSetMapper;
import com.viettel.bccs.productcatalog.optionset.repository.OptionSetRepository;
import com.viettel.bccs.productcatalog.utils.RequestValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OptionSetService {

    private final OptionSetRepository optionSetRepository;
    private final OptionSetMapper optionSetMapper;

    @Transactional(readOnly = true)
    public OptionSetResponse getByCode(String code) {
        RequestValidator.requireNotBlank(code, "code", "BCCS-PRODUCT-VALIDATE-0000");
        return optionSetRepository.findByCode(code)
                .map(optionSetMapper::toResponse)
                .orElseThrow(() -> new BusinessException("BCCS-CATALOG-OPTION-0001",
                        "Option set not found with code: " + code));
    }

    @Transactional(readOnly = true)
    public OptionSetResponse getById(Long id) {
        return optionSetRepository.findById(id)
                .map(optionSetMapper::toResponse)
                .orElseThrow(() -> new BusinessException("BCCS-CATALOG-OPTION-0001",
                        "Option set not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<OptionSetResponse> getAll() {
        return optionSetRepository.findAll().stream()
                .map(optionSetMapper::toResponse)
                .toList();
    }

}