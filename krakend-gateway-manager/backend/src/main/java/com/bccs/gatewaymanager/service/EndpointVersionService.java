package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.EndpointVersionSummaryDto;
import com.bccs.gatewaymanager.entity.EndpointChangeType;
import com.bccs.gatewaymanager.entity.EndpointConfig;
import com.bccs.gatewaymanager.entity.EndpointConfigVersion;
import com.bccs.gatewaymanager.exception.BusinessException;
import com.bccs.gatewaymanager.exception.SystemException;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import com.bccs.gatewaymanager.repository.EndpointConfigVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * Luu + doc lich su "phien ban" (snapshot toan bo EndpointConfig) tai moi lan
 * create/update/rollback, phuc vu man hinh "Lich su phien ban" va rollback.
 * KHONG goi nguoc lai EndpointService (tranh circular bean dependency -
 * EndpointService moi la noi phu thuoc VAO service nay, mot chieu) - toan bo
 * logic validate/save/cycle-check VAN nam nguyen o EndpointService.update(),
 * rollback() chi la 1 duong goi khac toi CUNG method do voi changeType khac.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EndpointVersionService {

    private final EndpointConfigVersionRepository repository;
    private final EndpointConfigRepository endpointConfigRepository;
    private final EndpointMapper mapper;
    private final ObjectMapper objectMapper;

    /**
     * Ghi 1 snapshot MOI cho endpoint vua duoc save thanh cong. Goi TRONG CUNG
     * transaction voi lan save entity chinh (EndpointService.create()/update()
     * deu @Transactional) - snapshot va thay doi thuc te luon nhat quan, khong
     * bao gio co truong hop save thanh cong nhung thieu version hoac nguoc lai.
     */
    @Transactional
    public void recordSnapshot(EndpointConfig savedEntity, EndpointChangeType changeType) {
        EndpointResponseDto dto = mapper.toResponseDto(savedEntity);
        int nextVersion = repository.findTopByEndpointIdOrderByVersionNumberDesc(savedEntity.getId())
                .map(v -> v.getVersionNumber() + 1)
                .orElse(1);

        EndpointConfigVersion version = EndpointConfigVersion.builder()
                .endpointId(savedEntity.getId())
                .versionNumber(nextVersion)
                .changeType(changeType)
                .name(dto.name())
                .path(dto.path())
                .method(dto.method())
                .snapshotJson(writeJson(dto))
                .build();
        repository.save(version);
        log.info("Da ghi version #{} ({}) cho endpoint id={}", nextVersion, changeType, savedEntity.getId());
    }

    @Transactional(readOnly = true)
    public List<EndpointVersionSummaryDto> listVersions(String endpointId) {
        requireOwnedByCurrentTeam(endpointId);
        return repository.findByEndpointIdOrderByVersionNumberDesc(endpointId).stream()
                .map(v -> new EndpointVersionSummaryDto(
                        v.getId(), v.getVersionNumber(), v.getChangeType(), v.getName(), v.getPath(), v.getMethod(), v.getCreatedAt()))
                .toList();
    }

    /** Xem chi tiet 1 phien ban (khong ap dung gi ca - chi de xem truoc khi quyet dinh Khoi phuc). */
    @Transactional(readOnly = true)
    public EndpointResponseDto getVersionDetail(String endpointId, String versionId) {
        requireOwnedByCurrentTeam(endpointId);
        EndpointConfigVersion version = getVersionOrThrow(endpointId, versionId);
        return readJson(version.getSnapshotJson());
    }

    /**
     * Chan IDOR: endpointId hop le nhung thuoc doi KHAC van phai bi tu choi giong
     * het endpointId khong ton tai - CAC METHOD nay duoc EndpointController goi
     * THANG (khong qua EndpointService.findOrThrow() da scoped), nen phai tu
     * kiem tra rieng o day, khong the dua vao tang tren.
     */
    private void requireOwnedByCurrentTeam(String endpointId) {
        endpointConfigRepository.findByIdAndTeamCode(endpointId, CurrentTeamContext.require())
                .orElseThrow(() -> new BusinessException("GW-404", "Khong tim thay endpoint id=" + endpointId));
    }

    /**
     * @return snapshot da chuyen thanh EndpointRequestDto (bo id/createdAt/updatedAt
     * - dung de goi lai EndpointService.update() y het luong sua thu cong qua UI).
     */
    @Transactional(readOnly = true)
    public EndpointRequestDto toRequestDtoForRollback(String endpointId, String versionId) {
        EndpointResponseDto snapshot = getVersionDetail(endpointId, versionId);
        return new EndpointRequestDto(
                snapshot.name(), snapshot.description(), snapshot.path(), snapshot.method(),
                snapshot.sequential(), snapshot.outputEncoding(),
                snapshot.steps().stream().map(this::stripStepId).toList(),
                snapshot.mappings(),
                snapshot.idempotencyEnabled(), snapshot.idempotencyTtlSeconds(), snapshot.parallelExecution(),
                snapshot.responseCacheEnabled(), snapshot.responseCacheTtlSeconds());
    }

    /**
     * BackendStepDto giu nguyen id cu tu snapshot se khien EndpointMapper.applySteps()
     * (qua BackendStep.builder() KHONG gan id) tao step MOI - Hibernate luon INSERT
     * (khong bao gio dung id cu de UPDATE, id chi duoc @Id gan khi entity moi duoc
     * tao). Giu id trong DTO rollback vi vay khong sai chuc nang, nhung de nham lan
     * neu doc lai code sau nay - bo ro rang cho dung voi toPayload() cua endpoint-form
     * (khong gui id trong payload tao/sua).
     */
    private BackendStepDto stripStepId(BackendStepDto s) {
        return new BackendStepDto(null, s.stepOrder(), s.name(), s.method(), s.urlPattern(),
                s.upstreamServiceId(), s.upstreamServiceName(), s.forwardOriginalBody(),
                s.cacheEnabled(), s.cacheTtlSeconds(), s.group(), s.target(),
                s.allowFields(), s.denyFields(), s.fieldRenameMapping(), s.canvasX(), s.canvasY(),
                s.connectTimeoutMs(), s.readTimeoutMs(),
                s.conditionSourceType(), s.conditionSourceStepOrder(), s.conditionSourceField(),
                s.conditionOperator(), s.conditionExpectedValue(),
                s.nextStepOrderIfTrue(), s.nextStepOrderIfFalse(), s.onErrorStepOrder(), s.parallelGroup(),
                s.compensationUpstreamServiceId(), s.compensationUpstreamServiceName(),
                s.compensationMethod(), s.compensationUrlPattern());
    }

    /** Dung khi xoa han 1 endpoint - don toan bo lich su phien ban cua no (khong FK/cascade tu dong, xem EndpointConfigVersion). */
    @Transactional
    public void deleteAllForEndpoint(String endpointId) {
        long deleted = repository.deleteByEndpointId(endpointId);
        log.info("Da xoa {} version cua endpoint id={}", deleted, endpointId);
    }

    private EndpointConfigVersion getVersionOrThrow(String endpointId, String versionId) {
        EndpointConfigVersion version = repository.findById(versionId)
                .orElseThrow(() -> new BusinessException("GW-VERSION-404", "Khong tim thay phien ban id=" + versionId));
        // Khong tiet lo version thuoc endpoint khac co ton tai hay khong - tra ve
        // dung 1 loi "khong tim thay" chung cho ca 2 truong hop (versionId sai HOAC
        // versionId dung nhung thuoc endpoint khac), tranh ro ri thong tin qua IDOR.
        if (!version.getEndpointId().equals(endpointId)) {
            throw new BusinessException("GW-VERSION-404", "Khong tim thay phien ban id=" + versionId + " cua endpoint id=" + endpointId);
        }
        return version;
    }

    private String writeJson(EndpointResponseDto dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (RuntimeException e) {
            throw new SystemException("Khong the serialize snapshot phien ban endpoint id=" + dto.id(), e);
        }
    }

    private EndpointResponseDto readJson(String json) {
        try {
            return objectMapper.readValue(json, EndpointResponseDto.class);
        } catch (RuntimeException e) {
            throw new SystemException("Khong the doc snapshot phien ban (du lieu hong?)", e);
        }
    }
}
