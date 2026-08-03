package com.viettel.bccs.policy.reason.service;

import com.viettel.bccs.common.error.exception.BusinessException;
import com.viettel.bccs.policy.client.ProductPackageClient;
import com.viettel.bccs.policy.mapactiveinfo.dto.response.MapActiveInfoDTO;
import com.viettel.bccs.policy.mapactiveinfo.service.MapActiveInfoQuerryService;
import com.viettel.bccs.policy.reason.dto.response.ReasonDTO;
import com.viettel.bccs.policy.reason.dto.response.ReasonResponse;
import com.viettel.bccs.policy.utils.Const;
import com.viettel.bccs.policy.reason.entity.ReasonEntity;
import com.viettel.bccs.policy.reason.mapper.ReasonMapper;
import com.viettel.bccs.policy.reason.repository.ReasonRepository;
import com.viettel.bccs.policy.utils.DataUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReasonService {

    private final ReasonRepository repository;
    private final ReasonMapper mapper;
    private final ProductPackageClient productPackageClient;
    private MapActiveInfoQuerryService mapActiveInfoQuerryService;

    public ReasonResponse findById(Long id) {
        Optional<ReasonEntity> entity = repository.findById(id);
        if (entity.isEmpty()) {
            throw new BusinessException("BCCS-POLICY-001", "Reason not found with id: " + id);
        }
        return mapper.toResponse(entity.get());
    }

    public List<ReasonDTO> getListReasonByActionCodeAndTelServiceForAudit(
            String actionCode, Long telServiceId, String payType) {
        List<ReasonEntity> entities = repository.getListReasonByActionCodeAndTelServiceForAuditWithMappingChecking(actionCode, telServiceId, payType, null, true);
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

    private List<ReasonDTO> getListReasonByMapActiveInfoWithMappingChecking(List<MapActiveInfoDTO> mapActiveInfosDTO, Long numProduct, String productOfferType) {

        List<ReasonDTO> lstResult = new ArrayList<>();
        List<ReasonDTO> temp = new ArrayList<>();
        List<MapActiveInfoDTO> lstMapActiveInfoDTOtemp = new ArrayList<>();
        if (!DataUtil.isNullOrEmpty(mapActiveInfosDTO)) {
            String actionCode = mapActiveInfosDTO.get(0).getActionCode();
            List<ReasonDTO> lstReasonDTOs;
            log.info("getListReasonByMapActiveInfoWithMappingChecking. list reasonId from MapActiveInfo sau khi loc: " + mapActiveInfosDTO.stream().map(MapActiveInfoDTO::getRegReasonId).collect(Collectors.toList()));
            lstReasonDTOs = mapper.toDTO(repository.getByActionCodeOrderByIdWithMappingChecking(actionCode, mapActiveInfosDTO.get(0).getTelServiceId(), numProduct, productOfferType));
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
}