package com.viettel.bccs.organization.staffext.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.organization.staffext.dto.response.StaffExtResponse;
import com.viettel.bccs.organization.staffext.mapper.StaffExtMapper;
import com.viettel.bccs.organization.staffext.repository.StaffExtRepository;
import com.viettel.bccs.organization.utils.Const;
import com.viettel.bccs.organization.utils.DataUtil;
import com.viettel.bccs.organization.utils.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffExtService {

    private final StaffExtRepository staffExtRepository;
    private final StaffExtMapper staffExtMapper;

    @Cacheable(value = "staffExtCache", key = "'STAFF_ID:' + #staffId")
    @Transactional(readOnly = true)
    public List<StaffExtResponse> getByStaffId(Long staffId) {
        log.info("Lấy danh sách STAFF_EXT theo staffId: {}", staffId);
        return staffExtRepository.findByStaffId(staffId).stream()
                .map(staffExtMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "staffExtCache", key = "'STAFF_STATUS:' + #staffId + ':' + #status")
    @Transactional(readOnly = true)
    public List<StaffExtResponse> getByStaffIdAndStatus(Long staffId, String status) {
        RequestValidator.requireNotBlank(status, "status", "BCCS-PRODUCT-VALIDATE-0000");
        log.info("Lấy danh sách STAFF_EXT theo staffId: {} và status: {}", staffId, status);
        return staffExtRepository.findByStaffIdAndStatus(staffId, status).stream()
                .map(staffExtMapper::toResponse)
                .toList();
    }

    @Cacheable(value = "staffExtCache", key = "'STAFF_KEY:' + #staffId + ':' + #key")
    @Transactional(readOnly = true)
    public StaffExtResponse getStaffExtByStaffIDAndKey(Long staffId, String key) {
        RequestValidator.requireNotBlank(key, "key", "BCCS-PRODUCT-VALIDATE-0000");
        log.info("Lấy STAFF_EXT theo staffId: {} và key: {}", staffId, key);
        String checkValue = DataUtil.safeEqual(key, Const.StaffExtKey.BUSINESS_SPEC,
                Const.StaffExtKey.BUSINESS_TYPE_STAFF)
                ? "1" : null;
        return staffExtRepository.findByStaffIdAndKey(staffId, key, checkValue)
                .map(staffExtMapper::toResponse)
                .orElse(null);
    }
}
