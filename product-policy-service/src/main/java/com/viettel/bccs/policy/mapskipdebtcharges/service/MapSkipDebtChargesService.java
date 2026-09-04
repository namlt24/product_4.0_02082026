package com.viettel.bccs.policy.mapskipdebtcharges.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.policy.mapskipdebtcharges.dto.response.MapSkipDebtChargesDTO;
import com.viettel.bccs.policy.mapskipdebtcharges.dto.response.MapSkipDebtChargesDTOFull;
import com.viettel.bccs.policy.mapskipdebtcharges.mapper.MapSkipDebtChargesMapper;
import com.viettel.bccs.policy.mapskipdebtcharges.repository.MapSkipDebtChargesRepository;
import com.viettel.bccs.policy.utils.DataUtil;
import com.viettel.bccs.policy.utils.RequestValidator;
import com.viettel.bccs.policy.utils.ValidationPatterns;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapSkipDebtChargesService {

    private final MapSkipDebtChargesRepository repository;
    private final MapSkipDebtChargesMapper mapper;

    /**
     * Với mỗi phần tử đầu vào (dùng làm mẫu tìm kiếm - example), tìm các bản ghi MAP_SKIP_DEBT_CHARGES
     * khớp và gom vào 1 {@link MapSkipDebtChargesDTOFull}, key = vị trí (index) của mẫu trong danh
     * sách đầu vào để caller đối chiếu ngược lại phần tử nào sinh ra kết quả nào.
     */
    public List<MapSkipDebtChargesDTOFull> getFullInfo(List<
            MapSkipDebtChargesDTO> mapSkipDebtChargesDtos) throws Exception {
        List<MapSkipDebtChargesDTOFull> result = new ArrayList<>();
        if (DataUtil.isNullOrEmpty(mapSkipDebtChargesDtos)) {
            return result;
        }
        mapSkipDebtChargesDtos.forEach(this::validate);
        for (int i = 0; i < mapSkipDebtChargesDtos.size(); i++) {
            List<MapSkipDebtChargesDTO> findList = findByExample(mapSkipDebtChargesDtos.get(i));
            result.add(MapSkipDebtChargesDTOFull.builder()
                    .key(String.valueOf(i))
                    .mapSkipDebtChargesDTOList(findList)
                    .build());
        }
        return result;
    }

    private void validate(MapSkipDebtChargesDTO dto) {
        RequestValidator.checkRange(dto.getId(), "id", 0L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
        RequestValidator.checkRange(dto.getTelServiceId(), "telServiceId", 0L, 9999999999L,
                "BCCS-POLICY-VALIDATE-RANGE");
        RequestValidator.checkMaxLength(dto.getProductCode(), "productCode", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getProductCode(), "productCode", ValidationPatterns.CODE,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getProductName(), "productName", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getProductName(), "productName", ValidationPatterns.FREE_TEXT,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkRange(dto.getRegReasonId(), "regReasonId", 0L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
        RequestValidator.checkMaxLength(dto.getReasonName(), "reasonName", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getReasonName(), "reasonName", ValidationPatterns.FREE_TEXT,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkRange(dto.getChannelTypeId(), "channelTypeId", 0L, 9999999999L,
                "BCCS-POLICY-VALIDATE-RANGE");
        RequestValidator.checkMaxLength(dto.getChannelName(), "channelName", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getChannelName(), "channelName", ValidationPatterns.FREE_TEXT,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getProvinceCode(), "provinceCode", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getProvinceCode(), "provinceCode", ValidationPatterns.CODE,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getProvinceName(), "provinceName", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getProvinceName(), "provinceName", ValidationPatterns.FREE_TEXT,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getDistrictCode(), "districtCode", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getDistrictCode(), "districtCode", ValidationPatterns.CODE,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getDistrictName(), "districtName", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getDistrictName(), "districtName", ValidationPatterns.FREE_TEXT,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getPrecinctCode(), "precinctCode", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getPrecinctCode(), "precinctCode", ValidationPatterns.CODE,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getPrecinctName(), "precinctName", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getPrecinctName(), "precinctName", ValidationPatterns.FREE_TEXT,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getShopCode(), "shopCode", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getShopCode(), "shopCode", ValidationPatterns.CODE,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getStaffCode(), "staffCode", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getStaffCode(), "staffCode", ValidationPatterns.CODE,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getActionCode(), "actionCode", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getActionCode(), "actionCode", ValidationPatterns.CODE,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getActionName(), "actionName", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getActionName(), "actionName", ValidationPatterns.FREE_TEXT,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getStatus(), "status", 1, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getStatus(), "status", ValidationPatterns.ALPHANUMERIC,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getCreateUser(), "createUser", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getCreateUser(), "createUser", ValidationPatterns.USER_CODE,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getUpdateUser(), "updateUser", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getUpdateUser(), "updateUser", ValidationPatterns.USER_CODE,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkRange(dto.getCycle(), "cycle", 0L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
        RequestValidator.checkMaxLength(dto.getSkipHotCharges(), "skipHotCharges", 2, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getSkipHotCharges(), "skipHotCharges", ValidationPatterns.ALPHANUMERIC,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getPayType(), "payType", 2, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getPayType(), "payType", ValidationPatterns.ALPHANUMERIC,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkRange(dto.getOfferId(), "offerId", 0L, 9999999999L, "BCCS-POLICY-VALIDATE-RANGE");
        RequestValidator.checkRange(dto.getCustGroupId(), "custGroupId", 0L, 9L, "BCCS-POLICY-VALIDATE-RANGE");
        RequestValidator.checkMaxLength(dto.getCustType(), "custType", 10, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getCustType(), "custType", ValidationPatterns.CODE,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getCustIdNo(), "custIdNo", 50, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getCustIdNo(), "custIdNo", ValidationPatterns.FREE_TEXT,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getCustAccountNo(), "custAccountNo", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getCustAccountNo(), "custAccountNo", ValidationPatterns.FREE_TEXT,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getCustCode(), "custCode", 100, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getCustCode(), "custCode", ValidationPatterns.CODE,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getActStatus(), "actStatus", 3, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getActStatus(), "actStatus", ValidationPatterns.ALPHANUMERIC,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getSkipLastSub(), "skipLastSub", 2, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getSkipLastSub(), "skipLastSub", ValidationPatterns.ALPHANUMERIC,
                "BCCS-POLICY-VALIDATE-PATTERN");
        RequestValidator.checkMaxLength(dto.getSkipContract(), "skipContract", 2, "BCCS-POLICY-VALIDATE-SIZE");
        RequestValidator.checkPattern(dto.getSkipContract(), "skipContract", ValidationPatterns.ALPHANUMERIC,
                "BCCS-POLICY-VALIDATE-PATTERN");
    }

    private List<MapSkipDebtChargesDTO> findByExample(MapSkipDebtChargesDTO example) throws Exception {
        try {
            List<MapSkipDebtChargesDTO> found = mapper.toDTO(repository.findByExample(example));
            return found == null ? new ArrayList<>() : found;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw ex;
        }
    }
}
