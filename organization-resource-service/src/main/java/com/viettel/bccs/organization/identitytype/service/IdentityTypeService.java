package com.viettel.bccs.organization.identitytype.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.organization.client.OptionSetClient;
import com.viettel.bccs.organization.client.dto.OptionSetValueResponse;
import com.viettel.bccs.organization.custtype.service.CustTypeService;
import com.viettel.bccs.organization.identitytype.dto.IdentityTypeDTO;
import com.viettel.bccs.organization.identitytype.mapper.IdentityTypeMapper;
import com.viettel.bccs.organization.identitytype.repository.IdentityTypeRepository;
import com.viettel.bccs.organization.utils.Const;
import com.viettel.bccs.organization.utils.RequestValidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdentityTypeService {

    private final IdentityTypeRepository identityTypeRepository;
    private final IdentityTypeMapper identityTypeMapper;
    private final CustTypeService custTypeService;
    private final OptionSetClient optionSetClient;

    @Transactional(readOnly = true)
    public List<IdentityTypeDTO> findAllActive() {
        log.info("Truy vấn tất cả loại giấy tờ đang hiệu lực");
        return identityTypeRepository.findAllByStatus(Const.Status.ACTIVE)
                .stream()
                .map(identityTypeMapper::toDTO)
                .toList();
    }

    @Cacheable(value = "identityTypeCache", key = "'ID_TYPE:' + #idType")
    @Transactional(readOnly = true)
    public IdentityTypeDTO findByIdType(String idType) {
        RequestValidator.requireNotBlank(idType, "idType", "BCCS-PRODUCT-VALIDATE-0000");
        log.info("Truy vấn loại giấy tờ theo mã: {}", idType);
        return identityTypeRepository.findByIdTypeAndStatus(idType, Const.Status.ACTIVE)
                .map(identityTypeMapper::toDTO)
                .orElseThrow(() -> new BusinessException("BCCS-ORGANIZATION-IDENTITYTYPE-0001",
                        "Không tìm thấy loại giấy tờ với mã: " + idType));
    }

    // TODO: re-enable cache after fixing Redis serializer compatibility with product-catalog-service
    // @Cacheable(value = "identityTypeCache", key = "'IDENTITY_TYPE_LIST:' + #custType")
    @Transactional(readOnly = true)
    public List<IdentityTypeDTO> getListIdentityType(String custType) {
        log.info("Lấy danh sách loại giấy tờ theo loại khách hàng: {}", custType);

        var custTypeDTO = custTypeService.findActiveByCustType(custType);
        String groupType = custTypeDTO.getGroupType();

        String optionSetCode = Const.OPTION_SET_CODE_LIST_IDENTITY_GROUPTYPE_ + groupType;
        List<OptionSetValueResponse> lstOptionSetValue = optionSetClient.findValueByOptionSetCode(optionSetCode);

        List<IdentityTypeDTO> lstAllIdentity = findAllActive();

        if (!lstOptionSetValue.isEmpty()) {
            List<String> lstIdentityConfig = lstOptionSetValue.stream()
                    .map(OptionSetValueResponse::getValue)
                    .toList();
            return lstAllIdentity.stream()
                    .filter(x -> lstIdentityConfig.contains(x.getIdType()))
                    .toList();
        }

        List<String> lstIdentityConfig = switch (groupType) {
            case "1", "3" -> List.of("IDC", "PASS");
            case "2" -> List.of("BUS", "TIN");
            default -> List.of();
        };

        return lstAllIdentity.stream()
                .filter(x -> lstIdentityConfig.contains(x.getIdType()))
                .toList();
    }
}
