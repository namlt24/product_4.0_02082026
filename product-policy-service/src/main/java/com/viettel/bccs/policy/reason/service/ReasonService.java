package com.viettel.bccs.policy.reason.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.client.OptionSetClient;
import com.viettel.bccs.policy.client.ProductPackageClient;
import com.viettel.bccs.policy.client.ProductSpecCharClient;
import com.viettel.bccs.policy.client.dto.OptionSetValueResponse;
import com.viettel.bccs.policy.client.dto.ProductSpecCharLookupDTO;
import com.viettel.bccs.policy.client.dto.ProductSpecCharValueLookupDTO;
import com.viettel.bccs.policy.client.dto.StaffDTO;
import com.viettel.bccs.policy.common.dto.FilterRequest;
import com.viettel.bccs.policy.common.helper.StaffResolveHelper;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoQuerryService;
import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
import com.viettel.bccs.policy.reasoncharuse.dto.response.ReasonCharUseDTO;
import com.viettel.bccs.policy.reasoncharuse.mapper.ReasonCharUseMapper;
import com.viettel.bccs.policy.reasoncharuse.repository.ReasonCharUseRepository;
import com.viettel.bccs.policy.reasonpause.dto.response.ReasonPauseDTO;
import com.viettel.bccs.policy.reasonpause.service.ReasonPauseService;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.reason.entity.ReasonEntity;
import com.viettel.bccs.policy.reason.mapper.ReasonMapper;
import com.viettel.bccs.policy.reason.repository.ReasonRepository;
import com.viettel.bccs.policy.utils.DataUtil;
import com.viettel.bccs.policy.utils.RequiredRoleMap;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class ReasonService {

    private final ReasonRepository repository;
    private final ReasonMapper mapper;
    private final ProductPackageClient productPackageClient;
    private final ReasonCharUseRepository reasonCharUseRepository;
    private final ReasonCharUseMapper reasonCharUseMapper;
    private final ProductSpecCharClient productSpecCharClient;
    private final OptionSetClient optionSetClient;
    private final ReasonPauseService reasonPauseService;
    @Lazy
    private final MapActiveInfoQuerryService mapActiveInfoQuerryService;
    private final StaffResolveHelper staffResolveHelper;

    public ReasonService(ReasonRepository repository, ReasonMapper mapper,
                          ProductPackageClient productPackageClient,
                          ReasonCharUseRepository reasonCharUseRepository, ReasonCharUseMapper reasonCharUseMapper,
                          ProductSpecCharClient productSpecCharClient, OptionSetClient optionSetClient,
                          ReasonPauseService reasonPauseService,
                          @Lazy MapActiveInfoQuerryService mapActiveInfoQuerryService,
                          StaffResolveHelper staffResolveHelper) {
        this.repository = repository;
        this.mapper = mapper;
        this.productPackageClient = productPackageClient;
        this.reasonCharUseRepository = reasonCharUseRepository;
        this.reasonCharUseMapper = reasonCharUseMapper;
        this.productSpecCharClient = productSpecCharClient;
        this.optionSetClient = optionSetClient;
        this.reasonPauseService = reasonPauseService;
        this.mapActiveInfoQuerryService = mapActiveInfoQuerryService;
        this.staffResolveHelper = staffResolveHelper;
    }

    public ReasonResponse findById(Long id) {
        Optional<ReasonEntity> entity = repository.findById(id);
        if (entity.isEmpty()) {
            throw new BusinessException("BCCS-POLICY-REASON-0001", "Reason not found with id: " + id);
        }
        return mapper.toResponse(entity.get());
    }

    public List<ReasonDTO> getListReasonByActionCodeAndTelServiceForAudit(
            String actionCode, Long telServiceId, String payType) {
        List<ReasonEntity> entities = repository.getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(actionCode, telServiceId, payType, null, true, List.of());
        return entities.stream()
                .map(mapper::toDTO)
                .toList();
    }

    public List<ReasonDTO> getReasonFromMapActiveInfos(List<MapActiveInfoDTO> mapActiveInfosDTO, int mode, Long numOffer) {
        if (DataUtil.isNullOrEmpty(mapActiveInfosDTO)) {
            return new ArrayList<>();
        }
        List<MapActiveInfoDTO> tempMapActiveInfos = mapActiveInfoQuerryService.getMapActiveInfosByLevel(mapActiveInfosDTO, "regReasonId", mode);
        return getListReasonByMapActiveInfoWithMappingChecking(tempMapActiveInfos, numOffer, null);
    }

    private List<String> resolveExcludeProdOfferTypeIds(Long numProduct) {
        if (numProduct == null) {
            return List.of();
        }
        return optionSetClient.findValueByOptionSetCode(Const.PRODUCT_PACKAGE.EXCLUDE_PROD_OFFER_TYPE_ID).stream()
                .map(OptionSetValueResponse::getValue)
                .toList();
    }

    private List<ReasonDTO> getListReasonByMapActiveInfoWithMappingChecking(List<MapActiveInfoDTO> mapActiveInfosDTO, Long numProduct, String productOfferType) {

        List<ReasonDTO> lstResult = new ArrayList<>();
        List<ReasonDTO> temp = new ArrayList<>();
        List<MapActiveInfoDTO> lstMapActiveInfoDTOtemp = new ArrayList<>();
        if (!DataUtil.isNullOrEmpty(mapActiveInfosDTO)) {
            String actionCode = mapActiveInfosDTO.get(0).getActionCode();
            List<ReasonDTO> lstReasonDTOs;
            log.info("getListReasonByMapActiveInfoWithMappingChecking. list reasonId from MapActiveInfo sau khi loc: " + mapActiveInfosDTO.stream().map(MapActiveInfoDTO::getRegReasonId).collect(Collectors.toList()));
            List<String> excludeProdOfferTypeIds = resolveExcludeProdOfferTypeIds(numProduct);
            lstReasonDTOs = mapper.toDTO(repository.getByActionCodeOrderByIdWithMappingChecking(actionCode, mapActiveInfosDTO.get(0).getTelServiceId(), numProduct, productOfferType, excludeProdOfferTypeIds));
            log.info("list reasonId after select DB : " + lstReasonDTOs.stream().map(ReasonDTO::getReasonId).collect(Collectors.toList()));
            for (MapActiveInfoDTO mapActiveInfoDTO : mapActiveInfosDTO) {
                if (DataUtil.safeEqual(-1, mapActiveInfoDTO.getRegReasonId())) {
                    //do trong list reason xem co thoa man dieu kien ve tel_service va pay_type
                    for (ReasonDTO reasonDTO : lstReasonDTOs) {
                        if (mapActiveInfoDTO.getPayType().equals(reasonDTO.getPayType())
                                && (DataUtil.safeEqual(-1, mapActiveInfoDTO.getTelServiceId())
                                || ("," + reasonDTO.getTelService() + ",").contains("," + mapActiveInfoDTO.getTelServiceId() + ","))) {
                            //thoa man dieu kien, add vao danh sach tra ve
                            lstResult.add(reasonDTO);
                        } else {
                            //add vao danh sach tam de xu ly sau
                            temp.add(reasonDTO);
                        }
                    }
                    lstReasonDTOs.clear();
                    lstReasonDTOs.addAll(temp);
                    temp.clear();
                } else {
                    lstMapActiveInfoDTOtemp.add(mapActiveInfoDTO);
                }
            }

            int indexReason = 0;
            int indexMapActiveInfo = 0;
            int maxReasonIndex = lstReasonDTOs.size();
            int maxMapActiveInfo = lstMapActiveInfoDTOtemp.size();

            while (true) {
                if ((indexReason >= maxReasonIndex)
                        || (indexMapActiveInfo >= maxMapActiveInfo)) {
                    break;
                }
                ReasonDTO currentReason = lstReasonDTOs.get(indexReason);
                MapActiveInfoDTO currentMap = lstMapActiveInfoDTOtemp.get(indexMapActiveInfo);
                if (DataUtil.safeEqual(currentReason.getReasonId(), currentMap.getRegReasonId())) {
                    if (DataUtil.safeEqual(currentMap.getPayType(), currentReason.getPayType())
                            && (DataUtil.safeEqual(-1, currentMap.getTelServiceId())
                            || ("," + currentReason.getTelService() + ",").contains("," + currentMap.getTelServiceId() + ","))) {
                        lstResult.add(lstReasonDTOs.get(indexReason));
                    }
                    indexReason++;
                    indexMapActiveInfo++;
                } else if (currentReason.getReasonId() > currentMap.getRegReasonId()) {
                    indexMapActiveInfo++;
                } else if (currentMap.getRegReasonId() > currentReason.getReasonId()) {
                    indexReason++;
                }

            }
        }
        return lstResult;
    }

    public List<ReasonDTO> getReasonFromMapActiveInfosForVas(List<MapActiveInfoDTO> mapActiveInfosDTO, int mode, Long numOffer) {
        if (DataUtil.isNullOrEmpty(mapActiveInfosDTO)) {
            return new ArrayList<>();
        }
        List<MapActiveInfoDTO> tempMapActiveInfos = mapActiveInfoQuerryService.getMapActiveInfosByLevel(mapActiveInfosDTO, "regReasonId", mode);
        //tach thanh ham goi sang ReasonService
        return getListReasonByMapActiveInfoWithMappingChecking(tempMapActiveInfos, numOffer, Const.PRODUCT_OFFER_TYPE.VAS);
    }

    public List<ReasonDTO> getReasonFull(String staffCode, String payType, Long offerId, String actionCode, String serviceType, String province, String district, String precint, String customerGroup, String customerType, String subType, String subGroup, String stationCodes, String promotionCode, String technology, Integer mode, Boolean getReasonCharUse, RequiredRoleMap roleMap, String nodeCode, Long singleOrCombo, List<FilterRequest> listProductSpec, List<String> lstBusinessNo) {
        StaffDTO staffDTO = staffResolveHelper.resolveStaffDTO(staffCode);

        List<ReasonDTO> lstResult = mapActiveInfoQuerryService.getReasonFullWithBusinessNo(staffDTO, payType, offerId, actionCode, serviceType, province, district, precint, customerGroup, customerType, subType, subGroup, stationCodes, promotionCode, technology, mode, getReasonCharUse, roleMap, nodeCode, singleOrCombo, listProductSpec, false, lstBusinessNo);
        if (!DataUtil.isNullOrEmpty(lstResult)) {
            lstResult.sort(Comparator.comparing(ReasonDTO::getReasonCode));
        }

        return lstResult;
    }

    public List<ReasonDTO> getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(String actionCode, Long telecomServiceId,
                                                                                             String payType, Long numProduct, boolean checkStatus) {
        List<ReasonEntity> entities = repository.getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(actionCode, telecomServiceId,
                payType, numProduct, checkStatus, resolveExcludeProdOfferTypeIds(numProduct)
        );
        return mapper.toDTO(entities);
    }

    public List<ReasonDTO> findByLstIdWithSpec(List<Long> lstReasonId, List<FilterRequest> listProductSpec, String productCode) {
        return repository.findByLstIdWithSpec(lstReasonId, listProductSpec, productCode);
    }

    public List<ReasonDTO> getReasonCharUse(List<ReasonDTO> lstReason) {
        if (DataUtil.isNullOrEmpty(lstReason)) {
            return lstReason;
        }
        List<Long> reasonIds = lstReason.stream().map(ReasonDTO::getReasonId).collect(Collectors.toList());
        List<ReasonCharUseDTO> charUses = reasonCharUseMapper.toDTO(
                reasonCharUseRepository.findByReasonIdInAndStatus(reasonIds, Const.STATUS.ACTIVE));
        if (DataUtil.isNullOrEmpty(charUses)) {
            return lstReason;
        }

        List<Long> specCharIds = charUses.stream()
                .map(ReasonCharUseDTO::getProductSpecCharId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        List<Long> specCharValueIds = charUses.stream()
                .map(ReasonCharUseDTO::getProductSpecCharValueId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> codeBySpecCharId = productSpecCharClient.findByIds(specCharIds).stream()
                .collect(Collectors.toMap(ProductSpecCharLookupDTO::getProductSpecCharId, ProductSpecCharLookupDTO::getCode, (a, b) -> a));
        Map<Long, String> valueBySpecCharValueId = productSpecCharClient.findValuesByIds(specCharValueIds).stream()
                .collect(Collectors.toMap(ProductSpecCharValueLookupDTO::getProductSpecCharValueId, ProductSpecCharValueLookupDTO::getValue, (a, b) -> a));

        Map<Long, List<ReasonCharUseDTO>> charUseByReasonId = new HashMap<>();
        for (ReasonCharUseDTO charUse : charUses) {
            String code = codeBySpecCharId.get(charUse.getProductSpecCharId());
            if (code == null) {
                continue;
            }
            String value = charUse.getProductSpecCharValueId() != null
                    ? valueBySpecCharValueId.get(charUse.getProductSpecCharValueId())
                    : null;
            charUseByReasonId.computeIfAbsent(charUse.getReasonId(), k -> new ArrayList<>())
                    .add(ReasonCharUseDTO.builder().code(code).value(value).build());
        }

        for (ReasonDTO reason : lstReason) {
            reason.setLstCharUse(charUseByReasonId.getOrDefault(reason.getReasonId(), Collections.emptyList()));
        }
        return lstReason;
    }

    public Map<Long, List<ReasonPauseDTO>> getReasonPauseByReasonIds(List<Long> reasonIds) {
        return reasonPauseService.getReasonPauseByReasonIds(reasonIds);
    }

    public boolean checkAttReason(Long reasonId, String attributeCode) {
        if (DataUtil.isAnyNull(reasonId, attributeCode)) {
            throw new BusinessException("BCCS-POLICY-REASON-0003", "reasonId and attributeCode are required");
        }
        List<ReasonCharUseDTO> charUses = reasonCharUseMapper.toDTO(
                reasonCharUseRepository.findByReasonIdInAndStatus(List.of(reasonId), Const.STATUS.ACTIVE));
        if (DataUtil.isNullOrEmpty(charUses)) {
            return false;
        }

        List<Long> specCharIds = charUses.stream()
                .map(ReasonCharUseDTO::getProductSpecCharId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (specCharIds.isEmpty()) {
            return false;
        }

        return productSpecCharClient.findByIds(specCharIds).stream()
                .anyMatch(dto -> attributeCode.equals(dto.getCode()));
    }
}
