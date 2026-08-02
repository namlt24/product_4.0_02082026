package com.viettel.bccs.organization.staffext.service;

import com.viettel.bccs.organization.staffext.dto.response.StaffExtResponse;
import com.viettel.bccs.organization.staffext.mapper.StaffExtMapper;
import com.viettel.bccs.organization.staffext.repository.StaffExtRepository;
import com.viettel.bccs.organization.utils.Const;
import com.viettel.bccs.organization.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffExtService {

    private final StaffExtRepository staffExtRepository;
    private final StaffExtMapper staffExtMapper;

    @Transactional(readOnly = true)
    public List<StaffExtResponse> getByStaffId(Long staffId) {
        log.info("Lấy danh sách STAFF_EXT theo staffId: {}", staffId);
        return staffExtRepository.findByStaffId(staffId).stream()
                .map(staffExtMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StaffExtResponse> getByStaffIdAndStatus(Long staffId, String status) {
        log.info("Lấy danh sách STAFF_EXT theo staffId: {} và status: {}", staffId, status);
        return staffExtRepository.findByStaffIdAndStatus(staffId, status).stream()
                .map(staffExtMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StaffExtResponse getStaffExtByStaffIDAndKey(Long staffId, String key) {
        log.info("Lấy STAFF_EXT theo staffId: {} và key: {}", staffId, key);
        String checkValue = DataUtil.safeEqual(key, Const.STAFF_EXT_KEY.BUSINESS_SPEC, Const.STAFF_EXT_KEY.BUSINESS_TYPE_STAFF)
                ? "1" : null;
        return staffExtRepository.findByStaffIdAndKey(staffId, key, checkValue)
                .map(staffExtMapper::toResponse)
                .orElse(null);
    }
}