package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.config.CurrentTeamContext;
import com.bccs.gatewaymanager.dto.BackendStepDto;
import com.bccs.gatewaymanager.dto.ConfigExportDto;
import com.bccs.gatewaymanager.dto.ConfigImportResultDto;
import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.FieldMappingDto;
import com.bccs.gatewaymanager.dto.FlagDto;
import com.bccs.gatewaymanager.dto.UpstreamServiceDto;
import com.bccs.gatewaymanager.entity.GwmFlag;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import com.bccs.gatewaymanager.repository.EndpointConfigVersionRepository;
import com.bccs.gatewaymanager.repository.GwmFlagRepository;
import com.bccs.gatewaymanager.repository.UpstreamServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Export/Import toan bo cau hinh (P1) - backup, review qua Pull Request, hoac
 * dong bo giua 2 moi truong (vi du dev -> staging).
 *
 * Import la UPSERT (khong bao gio XOA gi): Upstream khop theo TEN, Endpoint
 * khop theo PATH (2 field nay co rang buoc UNIQUE that trong DB - dung lam
 * khoa doi chieu dang tin cay hon id, vi id co the khac nhau giua 2 moi
 * truong khac nhau du cung 1 "endpoint logic"). Toan bo import chay trong 1
 * TRANSACTION DUY NHAT - 1 endpoint trong bundle bi loi (vi du sai du lieu)
 * se ROLLBACK TOAN BO import, khong ap dung nua-vơi (an toan hon cho 1 thao
 * tac "restore cau hinh" - tranh trang thai lung chung kho doan).
 */
@Slf4j
@Service
@Profile("control-plane")
@RequiredArgsConstructor
public class ConfigExportImportService {

    private static final String SCHEMA_VERSION = "1.0";

    private final UpstreamServiceRepository upstreamRepository;
    private final UpstreamServiceService upstreamService;
    private final EndpointConfigRepository endpointRepository;
    private final EndpointService endpointService;
    private final EndpointMapper endpointMapper;
    private final EndpointConfigVersionRepository versionRepository;
    private final GwmFlagRepository flagRepository;

    /**
     * Dau van tay config CUA DUNG 1 DOI (CurrentTeamContext) cho co che ETag/304
     * cua sync DP->CP (RemoteConfigSyncService gui If-None-Match, CP tra 304 neu
     * khong doi) - GHEP 3 CAP so lieu tong hop (COUNT + MAX thoi gian) thay vi
     * hash payload:
     *  - upstream: COUNT + MAX(updated_at)  - bat create/update/delete upstream
     *  - endpoint: COUNT + MAX(updated_at)  - bat create/update/delete endpoint
     *  - version : COUNT + MAX(created_at)  - EndpointService.update() LUON ghi 1
     *    EndpointConfigVersion cung transaction (EndpointVersionService.recordSnapshot)
     *    nen cap nay bat duoc ca sua chi dong den step/mapping con - truong hop
     *    updated_at row endpoint cha khong chac nhay (replaceSteps clear+add lai toan bo con).
     *  - flag (V5) : COUNT + MAX(updated_at) GWM_FLAG - bat create/update/toggle
     *    (toggle chi doi enabled nhung @UpdateTimestamp van nhay) - DP biet luong
     *    theo cờ da doi <= 1 chu ky sync.
     * Moi duong ghi config deu qua EndpointService/UpstreamServiceService/FlagService
     * (UI CRUD + /api/config/import) nen 4 cap nay khong the khong nhay khi co thay doi.
     * Gia tri hash thay doi = config doi da doi -> DP can export lai (200);
     * giong lan truoc -> CP tra 304 (khong SELECT du lieu that, khong serialize).
     */
    @Transactional(readOnly = true)
    public String computeConfigHash() {
        String teamCode = CurrentTeamContext.require();
        // Query aggregate JPQL LUON tra dung 1 row (count=0, max=null khi doi chua co
        // gi) nen get(0) an toan. CAI BAI da gap that (2026-09-13): khai bao return
        // Object[] thi Spring Data do row-ngoai (do dai 1, phan tu [0] la Object[]
        // {count, max} ben trong) - truy cap [1] nem ArrayIndexOutOfBoundsException.
        Object[] upFp = upstreamRepository.fingerprintByTeamCode(teamCode).get(0);
        Object[] epFp = endpointRepository.fingerprintByTeamCode(teamCode).get(0);
        Object[] verFp = versionRepository.fingerprintByTeamCode(teamCode).get(0);
        Object[] flagFp = flagRepository.fingerprintByTeamCode(teamCode).get(0);
        String fingerprint = upFp[0] + "|" + upFp[1] + "|" + epFp[0] + "|" + epFp[1] + "|" + verFp[0] + "|" + verFp[1]
                + "|" + flagFp[0] + "|" + flagFp[1];
        return '"' + Integer.toHexString(fingerprint.hashCode()) + '"';
    }

    /**
     * Xuat TOAN BO cau hinh CUA DUNG 1 DOI (CurrentTeamContext) - day CHINH LA
     * kenh Data Plane cua doi do se dung de tu dong bo (xem
     * RemoteConfigSyncService): goi API nay bang chinh api_key cua doi minh,
     * luon chi nhan lai config cua doi minh, khong can API rieng nao khac.
     *
     * CO CHE ETag/304 (RFC 9110): neu client gui header If-None-Match trung voi
     * computeConfigHash() hien tai thi tra 304 Not Modified VOI body rong -
     * khong SELECT/serialize toan bo config (quan trong khi 1 doi co hang tram
     * endpoint: 99% chu ky sync 15s khong co gi thay doi). Khac thi tra 200 +
     * header ETag de lan sau client mang lai trong If-None-Match.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<ConfigExportDto> export(String ifNoneMatch) {
        String etag = computeConfigHash();
        if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
        }
        List<UpstreamServiceDto> upstreams = upstreamService.list();
        List<EndpointResponseDto> endpoints = endpointRepository.findAllByTeamCodeOrderByUpdatedAtDesc(CurrentTeamContext.require()).stream()
                .map(endpointMapper::toResponseDto)
                .toList();
        List<FlagDto> flags = flagRepository.findByTeamCode(CurrentTeamContext.require()).stream()
                .map(this::toFlagDto)
                .toList();
        return ResponseEntity.ok().eTag(etag).body(new ConfigExportDto(SCHEMA_VERSION, Instant.now(), upstreams, endpoints, flags));
    }

    @Transactional
    public ConfigImportResultDto importConfig(ConfigExportDto bundle) {
        Map<String, String> upstreamNameToId = new HashMap<>();
        for (UpstreamServiceDto existing : upstreamService.list()) {
            upstreamNameToId.put(existing.name(), existing.id());
        }

        int upstreamsCreated = 0;
        int upstreamsUpdated = 0;
        for (UpstreamServiceDto u : bundle.upstreams()) {
            if (upstreamNameToId.containsKey(u.name())) {
                upstreamService.update(upstreamNameToId.get(u.name()), u);
                upstreamsUpdated++;
            } else {
                UpstreamServiceDto created = upstreamService.create(u);
                upstreamNameToId.put(created.name(), created.id());
                upstreamsCreated++;
            }
        }

        // Import cờ TRUOC endpoint (endpoint tham chieu flagCode trong flag_rules -
        // EndpointService.validateFlagRules() se bao GW-FLAG-404 neu cờ chua ton tai).
        // Upsert khop (teamCode hien tai, flagCode); enabled/ten/mo ta lay theo bundle.
        int flagsCreated = 0;
        int flagsUpdated = 0;
        if (bundle.flags() != null) {
            for (FlagDto f : bundle.flags()) {
                var existing = flagRepository.findByTeamCodeAndFlagCodeIgnoreCase(CurrentTeamContext.require(), f.flagCode());
                if (existing.isPresent()) {
                    GwmFlag flag = existing.get();
                    flag.setDisplayName(f.displayName());
                    flag.setDescription(f.description());
                    flag.setEnabled(f.enabled());
                    flagRepository.save(flag);
                    flagsUpdated++;
                } else {
                    flagRepository.save(GwmFlag.builder()
                            .teamCode(CurrentTeamContext.require())
                            .flagCode(f.flagCode())
                            .displayName(f.displayName())
                            .description(f.description())
                            .enabled(f.enabled())
                            .build());
                    flagsCreated++;
                }
            }
        }

        List<String> warnings = new ArrayList<>();
        int endpointsCreated = 0;
        int endpointsUpdated = 0;
        for (EndpointResponseDto ep : bundle.endpoints()) {
            EndpointRequestDto request = toRequest(ep, upstreamNameToId, warnings);
            var existing = endpointRepository.findByTeamCodeAndPath(CurrentTeamContext.require(), ep.path());
            if (existing.isPresent()) {
                endpointService.update(existing.get().getId(), request);
                endpointsUpdated++;
            } else {
                endpointService.create(request);
                endpointsCreated++;
            }
        }

        log.info("Import cau hinh xong: {} upstream moi, {} upstream cap nhat, {} endpoint moi, {} endpoint cap nhat, {} cờ moi, {} cờ cap nhat, {} canh bao.",
                upstreamsCreated, upstreamsUpdated, endpointsCreated, endpointsUpdated, flagsCreated, flagsUpdated, warnings.size());
        return new ConfigImportResultDto(upstreamsCreated, upstreamsUpdated, endpointsCreated, endpointsUpdated, warnings);
    }

    private FlagDto toFlagDto(GwmFlag f) {
        return new FlagDto(f.getId(), f.getTeamCode(), f.getFlagCode(), f.getDisplayName(),
                f.getDescription(), f.isEnabled(), f.getCreatedAt(), f.getUpdatedAt());
    }

    /** Chuyen 1 snapshot EndpointResponseDto (tu bundle) thanh EndpointRequestDto de goi lai create()/update() - dung LAI toan bo validate/cycle-check co san. */
    private EndpointRequestDto toRequest(EndpointResponseDto ep, Map<String, String> upstreamNameToId, List<String> warnings) {
        List<BackendStepDto> steps = ep.steps().stream()
                .map(s -> {
                    String resolvedUpstreamId = upstreamNameToId.get(s.upstreamServiceName());
                    if (resolvedUpstreamId == null) {
                        warnings.add("Endpoint '" + ep.name() + "' - step '" + s.name() + "': khong tim thay Upstream ten '"
                                + s.upstreamServiceName() + "' trong bundle hoac trong DB hien tai, giu nguyen id cu (co the loi khi luu).");
                        resolvedUpstreamId = s.upstreamServiceId();
                    }
                    // Upstream bu tru (tuy chon) - CUNG rui ro portability nhu upstreamServiceId
                    // chinh o tren (id tu bundle co the khong ton tai o moi truong dich) - resolve
                    // lai theo TEN giong het, warn + giu id cu neu khong tim thay.
                    String resolvedCompensationUpstreamId = null;
                    if (s.compensationUpstreamServiceId() != null) {
                        resolvedCompensationUpstreamId = upstreamNameToId.get(s.compensationUpstreamServiceName());
                        if (resolvedCompensationUpstreamId == null) {
                            warnings.add("Endpoint '" + ep.name() + "' - step '" + s.name() + "': khong tim thay Upstream bu tru ten '"
                                    + s.compensationUpstreamServiceName() + "' trong bundle hoac trong DB hien tai, giu nguyen id cu (co the loi khi luu).");
                            resolvedCompensationUpstreamId = s.compensationUpstreamServiceId();
                        }
                    }
                    return new BackendStepDto(null, s.stepOrder(), s.name(), s.method(), s.urlPattern(),
                            resolvedUpstreamId, s.upstreamServiceName(), s.forwardOriginalBody(), s.cacheEnabled(),
                            s.cacheTtlSeconds(), s.group(), s.target(), s.allowFields(), s.denyFields(),
                            s.fieldRenameMapping(), s.canvasX(), s.canvasY(),
                            s.connectTimeoutMs(), s.readTimeoutMs(),
                            s.maxConcurrentCallsOverride(), s.maxWaitDurationMsOverride(),
                            // Dieu kien re nhanh (P1-5) giu nguyen tu bundle - la du lieu cau hinh
                            // thuan tuy (tham chieu stepOrder trong CUNG endpoint), khong gan voi
                            // moi truong nguon nhu upstreamServiceId nen khong can resolve lai gi.
                            s.conditionSourceType(), s.conditionSourceStepOrder(), s.conditionSourceField(),
                            s.conditionOperator(), s.conditionExpectedValue(), s.conditionFlagGroups(), s.conditionGroups(),
                            s.nextStepOrderIfTrue(), s.nextStepOrderIfFalse(), s.onErrorStepOrder(), s.parallelGroup(),
                            resolvedCompensationUpstreamId, s.compensationUpstreamServiceName(),
                            s.compensationMethod(), s.compensationUrlPattern(), s.switchBranches(),
                            s.falseErrorCode(), s.falseErrorMessage());
                })
                .toList();

        List<FieldMappingDto> mappings = ep.mappings().stream()
                .map(m -> new FieldMappingDto(null, m.sourceType(), m.sourceStepOrder(), m.sourceField(),
                        m.sourceArrayField(), m.sourceElementField(), m.constantValue(), m.targetStepOrder(), m.targetType(),
                        m.targetParamName(), m.mappingOrder(), m.targetContext(), m.filterField(), m.filterValue()))
                .toList();

        return new EndpointRequestDto(ep.name(), ep.description(), ep.path(), ep.method(),
                ep.sequential(), ep.outputEncoding(), steps, mappings,
                ep.idempotencyEnabled(), ep.idempotencyTtlSeconds(), ep.parallelExecution(),
                ep.responseCacheEnabled(), ep.responseCacheTtlSeconds(), ep.flagRules(),
                ep.responseMapping());
    }
}
