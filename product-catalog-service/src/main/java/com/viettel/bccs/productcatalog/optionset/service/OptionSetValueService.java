package com.viettel.bccs.productcatalog.optionset.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.productcatalog.client.CustTypeClient;
import com.viettel.bccs.productcatalog.client.dto.CustTypeDTO;
import com.viettel.bccs.productcatalog.optionset.dto.response.GetSubObjectResponse;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.entity.OptionSetValueEntity;
import com.viettel.bccs.productcatalog.optionset.mapper.OptionSetValueMapper;
import com.viettel.bccs.productcatalog.optionset.repository.OptionSetValueRepository;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import com.viettel.bccs.productcatalog.utils.RequestValidator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OptionSetValueService {

    private final OptionSetValueRepository optionSetValueRepository;
    private final OptionSetValueMapper optionSetValueMapper;
    private final CustTypeClient custTypeClient;

    @Transactional(readOnly = true)
    public List<OptionSetValueResponse> getByOptionSetId(Long optionSetId) {
        return optionSetValueRepository.findByOptionSetId(optionSetId).stream()
                .map(optionSetValueMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OptionSetValueResponse> getByOptionSetIdAndStatus(Long optionSetId, String status) {
        RequestValidator.requireNotBlank(status, "status", "BCCS-PRODUCT-VALIDATE-0000");
        return optionSetValueRepository.findByOptionSetIdAndStatus(optionSetId, status).stream()
                .map(optionSetValueMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OptionSetValueResponse> findByOptionSetCode(String code) {
        RequestValidator.requireNotBlank(code, "code", "BCCS-PRODUCT-VALIDATE-0000");
        return optionSetValueRepository.findByOptionSetCode(code).stream()
                .map(optionSetValueMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, List<OptionSetValueResponse>> findByOptionSetCodes(List<String> codes) {
        RequestValidator.requireNotEmpty(codes, "codes", "BCCS-PRODUCT-VALIDATE-0000");
        if (codes != null) {
            for (String code : codes) {
            }
        }
        List<Object[]> rows = optionSetValueRepository.findByOptionSetCodes(codes);
        return rows.stream()
                .map(row -> optionSetValueMapper.toResponse(
                        optionSetValueMapper.buildEntityFromRow(row),
                        row[11] != null ? row[11].toString().trim() : null))
                .toList()
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(OptionSetValueResponse::optionSetCode));
    }

    @Transactional(readOnly = true)
    public List<OptionSetValueResponse> getAllGroupCustType() {
        return optionSetValueRepository.findByOptionSetCode(Const.OptionSet.CUST_TYPE_GROUP_TYPE).stream()
                .map(optionSetValueMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public String getValueByTwoCodeOption(String optSetCode, String name) {
        RequestValidator.requireNotBlank(optSetCode, "optSetCode", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(name, "name", "BCCS-PRODUCT-VALIDATE-0000");
        return optionSetValueRepository.findValueByTwoCodeOption(optSetCode, name);
    }

    @Cacheable(value = "subObjectCache",
            key = "'SUB_OBJ:' + #custType + ':' + (#birthDate != null ? #birthDate : 'NULL')")
    public GetSubObjectResponse getSubObject(String custType, String birthDate) {
        RequestValidator.requireNotBlank(custType, "custType", "BCCS-PRODUCT-VALIDATE-0000");
        GetSubObjectResponse response = new GetSubObjectResponse();

        Optional<CustTypeDTO> custTypeOpt = custTypeClient.findActiveByCustType(custType, Const.Status.ACTIVE);
        if (custTypeOpt.isEmpty()) {
            response.setCode(Const.RestResponseCode.DATA_NOT_FOUND);
            return response;
        }

        String groupType = custTypeOpt.get().getGroupType();

        if (DataUtil.safeEqual(groupType, "1") || DataUtil.safeEqual(groupType, "3")) {
            int age = calculateAge(birthDate);
            if (age < 6) {
                response.setCode(Const.RestResponseCode.DATA_NOT_FOUND);
                return response;
            }
            if (age >= 16) {
                response.setLstOptionSetValue(findByOptionSetCode("SUB_OBJECT_OVER_16_INDIVIDUAL"));
                response.setNeedGuardianName(false);
            } else if (age >= 6 && age < 15) {
                response.setLstOptionSetValue(findByOptionSetCode("SUB_OBJECT_LESS_15_INDIVIDUAL"));
                response.setNeedGuardianName(true);
            } else if (age >= 15 && age < 16) {
                response.setLstOptionSetValue(findByOptionSetCode("SUB_OBJECT_LESS_16_INDIVIDUAL"));
                response.setNeedGuardianName(false);
            }
        } else if (DataUtil.safeEqual(groupType, "2")) {
            response.setLstOptionSetValue(findByOptionSetCode("SUB_OBJECT_BY_AGE_BUSINESS"));
        }

        response.setCode(Const.RestResponseCode.SUCCESS);
        return response;
    }

    private int calculateAge(String birthDateStr) {
        if (DataUtil.isNullOrEmpty(birthDateStr)) {
            return 0;
        }
        try {
            LocalDate birthDate = LocalDate.of(
                    Integer.parseInt(birthDateStr.substring(4, 8)),
                    Integer.parseInt(birthDateStr.substring(2, 4)),
                    Integer.parseInt(birthDateStr.substring(0, 2)));
            return Period.between(birthDate, LocalDate.now()).getYears();
        } catch (Exception e) {
            return 0;
        }
    }

    @Transactional(readOnly = true)
    public OptionSetValueResponse findOneByCodeAndValue(String code, String value) {
        List<OptionSetValueEntity> result = optionSetValueRepository.findByCodeAndValue(code, value);
        if (DataUtil.isNullOrEmpty(result)) {
            return null;
        }
        return optionSetValueMapper.toResponse(result.get(0));
    }
}