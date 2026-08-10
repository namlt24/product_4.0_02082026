package com.viettel.bccs.productcatalog.optionset.service;

import com.viettel.bccs.productcatalog.client.CustTypeClient;
import com.viettel.bccs.productcatalog.client.dto.CustTypeDTO;
import com.viettel.bccs.productcatalog.optionset.dto.response.OptionSetValueResponse;
import com.viettel.bccs.productcatalog.optionset.dto.response.ResponseMDealerDTO;
import com.viettel.bccs.productcatalog.optionset.entity.OptionSetValueEntity;
import com.viettel.bccs.productcatalog.optionset.mapper.OptionSetValueMapper;
import com.viettel.bccs.productcatalog.optionset.repository.OptionSetValueRepository;
import com.viettel.bccs.productcatalog.utils.Const;
import com.viettel.bccs.productcatalog.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        return optionSetValueRepository.findByOptionSetIdAndStatus(optionSetId, status).stream()
                .map(optionSetValueMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OptionSetValueResponse> findByOptionSetCode(String code) {
        return optionSetValueRepository.findByOptionSetCode(code).stream()
                .map(optionSetValueMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, List<OptionSetValueResponse>> findByOptionSetCodes(List<String> codes) {
        List<Object[]> rows = optionSetValueRepository.findByOptionSetCodes(codes);
        return rows.stream()
                .map(row -> optionSetValueMapper.toResponse(
                        buildEntityFromRow(row),
                        row[11] != null ? row[11].toString().trim() : null))
                .toList()
                .stream()
                .collect(java.util.stream.Collectors.groupingBy(OptionSetValueResponse::optionSetCode));
    }

    private OptionSetValueEntity buildEntityFromRow(Object[] row) {
        return OptionSetValueEntity.builder()
                .optionSetValueId(row[0] != null ? ((Number) row[0]).longValue() : null)
                .optionSetId(row[1] != null ? ((Number) row[1]).longValue() : null)
                .name(row[2] != null ? row[2].toString() : null)
                .value(row[3] != null ? row[3].toString() : null)
                .status(row[4] != null ? row[4].toString() : null)
                .description(row[5] != null ? row[5].toString() : null)
                .createUser(row[6] != null ? row[6].toString() : null)
                .createDatetime(row[7] instanceof java.sql.Date d ? d : null)
                .updateUser(row[8] != null ? row[8].toString() : null)
                .updateDatetime(row[9] instanceof java.sql.Date d ? d : null)
                .parentId(row[10] != null ? ((Number) row[10]).longValue() : null)
                .build();
    }

    @Transactional(readOnly = true)
    public List<OptionSetValueResponse> getAllGroupCustType() {
        return optionSetValueRepository.findByOptionSetCode(Const.OPTION_SET.CUST_TYPE_GROUP_TYPE).stream()
                .map(optionSetValueMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public String getValueByTwoCodeOption(String optSetCode, String name) {
        return optionSetValueRepository.findValueByTwoCodeOption(optSetCode, name);
    }

    @Cacheable(value = "mdealerSubObjectCache",
            key = "'SUB_OBJ:' + #custType + ':' + (#birthDate != null ? #birthDate : 'NULL')")
    public ResponseMDealerDTO getSubObject(String custType, String birthDate) {
        ResponseMDealerDTO response = new ResponseMDealerDTO();

        Optional<CustTypeDTO> custTypeOpt = custTypeClient.findActiveByCustType(custType, Const.STATUS.ACTIVE);
        if (custTypeOpt.isEmpty()) {
            response.setCode(Const.REST_RESPONSE_CODE.DATA_NOT_FOUND);
            return response;
        }

        String groupType = custTypeOpt.get().getGroupType();

        if (DataUtil.safeEqual(groupType, "1") || DataUtil.safeEqual(groupType, "3")) {
            int age = calculateAge(birthDate);
            if (age < 6) {
                response.setCode(Const.REST_RESPONSE_CODE.DATA_NOT_FOUND);
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

        response.setCode(Const.REST_RESPONSE_CODE.SUCCESS);
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