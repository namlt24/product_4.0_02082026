package com.viettel.bccs.policy.reason.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
import com.viettel.bccs.policy.reason.entity.ReasonEntity;
import com.viettel.bccs.policy.reason.mapper.ReasonMapper;
import com.viettel.bccs.policy.reason.repository.ReasonRepository;
import com.viettel.bccs.policy.reasoncharuse.dto.response.ReasonCharUseDTO;
import com.viettel.bccs.policy.reasoncharuse.mapper.ReasonCharUseMapper;
import com.viettel.bccs.policy.reasoncharuse.repository.ReasonCharUseRepository;
import com.viettel.bccs.policy.reasonpause.dto.response.ReasonPauseDTO;
import com.viettel.bccs.policy.reasonpause.service.ReasonPauseService;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.utils.DataUtil;
import com.viettel.bccs.policy.utils.RequestValidator;
import com.viettel.bccs.policy.utils.RequiredRoleMap;

import lombok.extern.slf4j.Slf4j;

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
    private final ReasonMappingLookupService reasonMappingLookupService;

    public ReasonService(ReasonRepository repository, ReasonMapper mapper,
                          ProductPackageClient productPackageClient,
                          ReasonCharUseRepository reasonCharUseRepository, ReasonCharUseMapper reasonCharUseMapper,
                          ProductSpecCharClient productSpecCharClient, OptionSetClient optionSetClient,
                          ReasonPauseService reasonPauseService,
                          @Lazy MapActiveInfoQuerryService mapActiveInfoQuerryService,
                          StaffResolveHelper staffResolveHelper,
                          ReasonMappingLookupService reasonMappingLookupService) {
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
        this.reasonMappingLookupService = reasonMappingLookupService;
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
        RequestValidator.requireNotBlank(actionCode, "actionCode", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotNull(telServiceId, "telServiceId", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(payType, "payType", "BCCS-PRODUCT-VALIDATE-0000");
        List<ReasonEntity> entities = repository.getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(
                actionCode, telServiceId, payType, null, true, List.of());
        return entities.stream()
                .map(mapper::toDTO)
                .toList();
    }

    public List<ReasonDTO> getReasonFromMapActiveInfos(List<MapActiveInfoDTO> mapActiveInfosDTO, int mode,
            Long numOffer) {
        if (DataUtil.isNullOrEmpty(mapActiveInfosDTO)) {
            return new ArrayList<>();
        }
        List<MapActiveInfoDTO> tempMapActiveInfos = mapActiveInfoQuerryService.getMapActiveInfosByLevel(
                mapActiveInfosDTO, "regReasonId", mode);
        return getListReasonByMapActiveInfoWithMappingChecking(tempMapActiveInfos, numOffer, null);
    }

    private List<String> resolveExcludeProdOfferTypeIds(Long numProduct) {
        if (numProduct == null) {
            return List.of();
        }
        return optionSetClient.findValueByOptionSetCode(Const.ProductPackage.EXCLUDE_PROD_OFFER_TYPE_ID).stream()
                .map(OptionSetValueResponse::getValue)
                .toList();
    }

    private List<ReasonDTO> getListReasonByMapActiveInfoWithMappingChecking(List<MapActiveInfoDTO> mapActiveInfosDTO,
            Long numProduct, String productOfferType) {

        List<ReasonDTO> lstResult = new ArrayList<>();
        List<ReasonDTO> temp = new ArrayList<>();
        List<MapActiveInfoDTO> lstMapActiveInfoDtoTemp = new ArrayList<>();
        if (!DataUtil.isNullOrEmpty(mapActiveInfosDTO)) {
            String actionCode = mapActiveInfosDTO.get(0).getActionCode();
            List<ReasonDTO> lstReasonDtos;
            log.info("getListReasonByMapActiveInfoWithMappingChecking. list reasonId from MapActiveInfo sau khi loc: " +
                    mapActiveInfosDTO.stream().map(MapActiveInfoDTO::getRegReasonId).collect(Collectors.toList()));
            List<String> excludeProdOfferTypeIds = resolveExcludeProdOfferTypeIds(numProduct);
            lstReasonDtos = new ArrayList<>(reasonMappingLookupService.getByActionCodeOrderByIdWithMappingChecking(
                    actionCode, mapActiveInfosDTO.get(0).getTelServiceId(), numProduct, productOfferType,
                            excludeProdOfferTypeIds));
            log.info("list reasonId after select DB : " + lstReasonDtos.stream().map(ReasonDTO::getReasonId).collect(
                    Collectors.toList()));
            for (MapActiveInfoDTO mapActiveInfoDTO : mapActiveInfosDTO) {
                if (DataUtil.safeEqual(-1, mapActiveInfoDTO.getRegReasonId())) {
                    //do trong list reason xem co thoa man dieu kien ve tel_service va pay_type
                    for (ReasonDTO reasonDTO : lstReasonDtos) {
                        if (mapActiveInfoDTO.getPayType().equals(reasonDTO.getPayType())
                                && (DataUtil.safeEqual(-1, mapActiveInfoDTO.getTelServiceId())
                                || ("," + reasonDTO.getTelService() + ",").contains("," +
                                        mapActiveInfoDTO.getTelServiceId() + ","))) {
                            //thoa man dieu kien, add vao danh sach tra ve
                            lstResult.add(reasonDTO);
                        } else {
                            //add vao danh sach tam de xu ly sau
                            temp.add(reasonDTO);
                        }
                    }
                    lstReasonDtos.clear();
                    lstReasonDtos.addAll(temp);
                    temp.clear();
                } else {
                    lstMapActiveInfoDtoTemp.add(mapActiveInfoDTO);
                }
            }

            int indexReason = 0;
            int indexMapActiveInfo = 0;
            int maxReasonIndex = lstReasonDtos.size();
            int maxMapActiveInfo = lstMapActiveInfoDtoTemp.size();

            while (true) {
                if ((indexReason >= maxReasonIndex)
                        || (indexMapActiveInfo >= maxMapActiveInfo)) {
                    break;
                }
                ReasonDTO currentReason = lstReasonDtos.get(indexReason);
                MapActiveInfoDTO currentMap = lstMapActiveInfoDtoTemp.get(indexMapActiveInfo);
                if (DataUtil.safeEqual(currentReason.getReasonId(), currentMap.getRegReasonId())) {
                    if (DataUtil.safeEqual(currentMap.getPayType(), currentReason.getPayType())
                            && (DataUtil.safeEqual(-1, currentMap.getTelServiceId())
                            || ("," + currentReason.getTelService() + ",").contains("," + currentMap.getTelServiceId() +
                                    ","))) {
                        lstResult.add(lstReasonDtos.get(indexReason));
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

    public List<ReasonDTO> getReasonFromMapActiveInfosForVas(List<MapActiveInfoDTO> mapActiveInfosDTO, int mode,
            Long numOffer) {
        if (DataUtil.isNullOrEmpty(mapActiveInfosDTO)) {
            return new ArrayList<>();
        }
        List<MapActiveInfoDTO> tempMapActiveInfos = mapActiveInfoQuerryService.getMapActiveInfosByLevel(
                mapActiveInfosDTO, "regReasonId", mode);
        //tach thanh ham goi sang ReasonService
        return getListReasonByMapActiveInfoWithMappingChecking(tempMapActiveInfos, numOffer,
                Const.ProductOfferType.VAS);
    }

    public List<ReasonDTO> getReasonFull(String staffCode, String payType, Long offerId, String actionCode,
            String serviceType, String province, String district, String precint, String customerGroup,
            String customerType, String subType, String subGroup, String stationCodes, String promotionCode,
            String technology, Integer mode, Boolean getReasonCharUse, RequiredRoleMap roleMap, String nodeCode,
            Long singleOrCombo, List<FilterRequest> listProductSpec, List<String> lstBusinessNo) {
        StaffDTO staffDTO = staffResolveHelper.resolveStaffDTO(staffCode);

        List<ReasonDTO> lstResult = mapActiveInfoQuerryService.getReasonFullWithBusinessNo(staffDTO, payType, offerId,
                actionCode, serviceType, province, district, precint, customerGroup, customerType, subType, subGroup,
                stationCodes, promotionCode, technology, mode, getReasonCharUse, roleMap, nodeCode, singleOrCombo,
                listProductSpec, false, lstBusinessNo);
        if (!DataUtil.isNullOrEmpty(lstResult)) {
            lstResult.sort(Comparator.comparing(ReasonDTO::getReasonCode));
        }

        return lstResult;
    }

    public List<ReasonDTO> getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(String actionCode,
            Long telecomServiceId,
            String payType,
            Long numProduct, boolean checkStatus) {
        List<ReasonEntity> entities = repository.getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(
                actionCode, telecomServiceId,
                payType, numProduct, checkStatus, resolveExcludeProdOfferTypeIds(numProduct)
        );
        return mapper.toDTO(entities);
    }

    public List<ReasonDTO> findByLstIdWithSpec(List<Long> lstReasonId, List<FilterRequest> listProductSpec,
            String productCode) {
        return repository.findByLstIdWithSpec(lstReasonId, listProductSpec, productCode);
    }

    public List<ReasonDTO> getReasonCharUse(List<ReasonDTO> lstReason) {
        if (DataUtil.isNullOrEmpty(lstReason)) {
            return lstReason;
        }
        List<Long> reasonIds = lstReason.stream().map(ReasonDTO::getReasonId).collect(Collectors.toList());
        List<ReasonCharUseDTO> charUses = reasonCharUseMapper.toDTO(
                reasonCharUseRepository.findByReasonIdInAndStatus(reasonIds, Const.Status.ACTIVE));
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
                .collect(Collectors.toMap(ProductSpecCharLookupDTO::getProductSpecCharId, ProductSpecCharLookupDTO::
                        getCode, (a, b) -> a));
        Map<Long, String> valueBySpecCharValueId = productSpecCharClient.findValuesByIds(specCharValueIds).stream()
                .collect(Collectors.toMap(ProductSpecCharValueLookupDTO::getProductSpecCharValueId,
                        ProductSpecCharValueLookupDTO::getValue, (a, b) -> a));

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

    /**
     * Lấy danh sách mã thuộc tính (product_spec_char.code) đang gán cho reasonId, qua REASON_CHAR_USE
     * (domain product-policy-service, JPA repository nội bộ) rồi resolve code qua ProductSpecChar
     * (domain product-catalog-service, gọi cross-service qua ProductSpecCharClient — khác domain nên
     * không join SQL trực tiếp). Cùng 2 nguồn dữ liệu, cùng cách resolve với checkAttReason ở trên,
     * chỉ khác: trả về toàn bộ danh sách mã thay vì kiểm tra đúng 1 mã.
     */
    public List<String> getReasonCharacter(Long reasonId) {
        RequestValidator.requireNotNull(reasonId, "reasonId", "BCCS-PRODUCT-VALIDATE-0000");
        if (DataUtil.isNullObject(reasonId)) {
            throw new BusinessException("BCCS-POLICY-REASON-0004", "reasonId is required");
        }
        List<ReasonCharUseDTO> charUses = reasonCharUseMapper.toDTO(
                reasonCharUseRepository.findByReasonIdInAndStatus(List.of(reasonId), Const.Status.ACTIVE));
        if (DataUtil.isNullOrEmpty(charUses)) {
            return List.of();
        }

        List<Long> specCharIds = charUses.stream()
                .map(ReasonCharUseDTO::getProductSpecCharId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (specCharIds.isEmpty()) {
            return List.of();
        }

        return productSpecCharClient.findByIds(specCharIds).stream()
                .map(ProductSpecCharLookupDTO::getCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }


    /**
     * Lấy danh sách giá trị thuộc tính (product_spec_char_value.value) của reasonId mà đặc tính đó có
     * code = specCode. Nguồn: REASON_CHAR_USE (domain policy, JPA nội bộ) nối sang ProductSpecChar /
     * PRODUCT_SPEC_CHAR_VALUE (domain catalog, gọi cross-service qua ProductSpecCharClient — khác domain
     * nên không join SQL trực tiếp). Nếu REASON_CHAR_USE lưu SPECIFIC_VALUE trực tiếp (không có
     * productSpecCharValueId) thì dùng giá trị đó. Không tìm thấy đặc tính khớp hoặc không có giá trị thì trả về rỗng.
     */
    public List<String> getValuesByReasonAndSpec(Long reasonId, String specCode) {
        RequestValidator.requireNotNull(reasonId, "reasonId", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(specCode, "specCode", "BCCS-PRODUCT-VALIDATE-0000");
        if (DataUtil.isAnyNull(reasonId, specCode)) {
            throw new BusinessException("BCCS-POLICY-REASON-0003", "reasonId and specCode are required");
        }

        List<ReasonCharUseDTO> charUses = reasonCharUseMapper.toDTO(
                reasonCharUseRepository.findByReasonIdInAndStatus(List.of(reasonId), Const.Status.ACTIVE));
        if (DataUtil.isNullOrEmpty(charUses)) {
            return List.of();
        }

        List<Long> specCharIds = charUses.stream()
                .map(ReasonCharUseDTO::getProductSpecCharId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (specCharIds.isEmpty()) {
            return List.of();
        }

        Set<Long> matchedSpecCharIds = productSpecCharClient.findByIds(specCharIds).stream()
                .filter(dto -> specCode.equals(dto.getCode()))
                .map(ProductSpecCharLookupDTO::getProductSpecCharId)
                .collect(Collectors.toSet());
        if (matchedSpecCharIds.isEmpty()) {
            return List.of();
        }

        List<ReasonCharUseDTO> matchedCharUses = charUses.stream()
                .filter(cu -> matchedSpecCharIds.contains(cu.getProductSpecCharId()))
                .toList();
        if (matchedCharUses.isEmpty()) {
            return List.of();
        }

        List<Long> specCharValueIds = matchedCharUses.stream()
                .map(ReasonCharUseDTO::getProductSpecCharValueId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<Long, String> valueBySpecCharValueId = specCharValueIds.isEmpty()
                ? Map.of()
                : productSpecCharClient.findValuesByIds(specCharValueIds).stream()
                        .collect(Collectors.toMap(ProductSpecCharValueLookupDTO::getProductSpecCharValueId,
                                ProductSpecCharValueLookupDTO::getValue, (a, b) -> a));

        return matchedCharUses.stream()
                .map(cu -> cu.getProductSpecCharValueId() != null
                        ? valueBySpecCharValueId.get(cu.getProductSpecCharValueId())
                        : cu.getSpecificValue())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    public Long getReasonIdByTypeAndCode(String reasonCode, String actionCode, Long telecomServiceId) {
        RequestValidator.requireNotBlank(reasonCode, "reasonCode", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(actionCode, "actionCode", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotNull(telecomServiceId, "telecomServiceId", "BCCS-PRODUCT-VALIDATE-0000");
        return repository.findReasonIdByCodeActionAndTelService(reasonCode, actionCode, telecomServiceId);
    }

    public boolean checkAttReason(Long reasonId, String attributeCode) {
        RequestValidator.requireNotNull(reasonId, "reasonId", "BCCS-PRODUCT-VALIDATE-0000");
        RequestValidator.requireNotBlank(attributeCode, "attributeCode", "BCCS-PRODUCT-VALIDATE-0000");
        if (DataUtil.isAnyNull(reasonId, attributeCode)) {
            throw new BusinessException("BCCS-POLICY-REASON-0003", "reasonId and attributeCode are required");
        }
        List<ReasonCharUseDTO> charUses = reasonCharUseMapper.toDTO(
                reasonCharUseRepository.findByReasonIdInAndStatus(List.of(reasonId), Const.Status.ACTIVE));
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
