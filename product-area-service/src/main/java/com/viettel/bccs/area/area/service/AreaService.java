package com.viettel.bccs.area.area.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.area.area.dto.response.AreaResponse;
import com.viettel.bccs.area.area.mapper.AreaMapper;
import com.viettel.bccs.area.area.repository.AreaRepository;
import com.viettel.bccs.area.utils.RequestValidator;
import com.viettel.bccs.common.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AreaService {

    private final AreaRepository areaRepository;
    private final AreaMapper areaMapper;

    @Transactional(readOnly = true)
    public List<AreaResponse> getAll() {
        return areaRepository.findAll().stream()
                .map(areaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public AreaResponse getByAreaCode(String areaCode) {
        RequestValidator.requireNotBlank(areaCode, "areaCode", "BCCS-AREA-VALIDATE-REQUIRED");
        return areaRepository.findByAreaCode(areaCode)
                .map(areaMapper::toResponse)
                .orElseThrow(() -> new BusinessException("BCCS-AREA-AREA-0001",
                        "Area not found with code: " + areaCode));
    }

    @Transactional(readOnly = true)
    public List<AreaResponse> getByParentCode(String parentCode) {
        return areaRepository.findByParentCode(parentCode).stream()
                .map(areaMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AreaResponse> getByProvince(String province) {
        RequestValidator.requireNotBlank(province, "province", "BCCS-AREA-VALIDATE-REQUIRED");
        return areaRepository.findByProvince(province).stream()
                .map(areaMapper::toResponse)
                .toList();
    }
}