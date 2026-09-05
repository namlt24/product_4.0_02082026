package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.EndpointRequestDto;
import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.dto.StepTraceDto;
import com.bccs.gatewaymanager.dto.TryResultDto;
import com.bccs.gatewaymanager.engine.CompositeOrchestratorEngine;
import com.bccs.gatewaymanager.engine.TraceCollector;
import com.bccs.gatewaymanager.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * "Thu ngay" (P1: cho endpoint DA LUU) + "Thu nhanh" (P2: cho 1 draft CHUA
 * LUU, dung khi khai bao qua Canvas - xem tryAdhoc()) - ca 2 deu goi THANG
 * CompositeOrchestratorEngine trong-process, dung Y HET engine ma Data Plane
 * that dung (DynamicDispatcherController), khong phai 1 ban gia lap rieng co
 * the lech hanh vi so voi luc chay that.
 *
 * Chay qua Control Plane (/api/**) nen KHONG bi RateLimitFilter gioi han (chi
 * ap cho Data Plane) va KHONG can CORS rieng (frontend da goi /api/** qua
 * nginx proxy san co).
 *
 * Ket qua tra ve la TryResultDto (envelope, xem javadoc rieng) thay vi de
 * loi throw thang ra GlobalExceptionHandler nhu ban dau: dung TraceCollector
 * (xem javadoc rieng - ThreadLocal trong-process, KHONG phu thuoc pipeline
 * audit Elasticsearch bat dong bo) de gom waterfall tung step, tra ve CA khi
 * that bai (hop DA chay truoc step loi van hien ra, khong phai man hinh loi
 * trang) - chi loi GOI SAI API (id khong ton tai, body khong phai JSON hop
 * le) moi throw binh thuong nhu truoc.
 */
@Service
@RequiredArgsConstructor
public class EndpointTryService {

    private final EndpointService endpointService;
    private final CompositeOrchestratorEngine engine;

    public TryResultDto tryCall(String endpointId, Map<String, String> pathVariables,
                                 Map<String, String> queryParams, String rawBody) {
        // endpointService.get() van throw GW-404 binh thuong (KHONG boc vao envelope) -
        // day la loi GOI SAI API (id sai), khong phai loi luc thuc thi.
        EndpointResponseDto config = endpointService.get(endpointId);
        return runWithTrace(null, () -> engine.handle(config, pathVariables, toQueryParamsArr(queryParams), rawBody));
    }

    /**
     * "Thu nhanh" cho 1 draft CHUA LUU (Canvas goi voi dung toPayload() hien
     * tai, khong can bam Luu truoc). Validate LAI dung 1 logic voi luc luu
     * that (EndpointService.validate()) truoc khi thuc thi - bat loi cau
     * hinh (stepOrder trung, branching sai...) NGAY, khong goi ra Upstream
     * nao ca neu draft khong hop le.
     */
    public TryResultDto tryAdhoc(EndpointRequestDto draft, Map<String, String> pathVariables,
                                  Map<String, String> queryParams, String rawBody) {
        EndpointResponseDto config = toAdhocResponseDto(draft);
        return runWithTrace(() -> endpointService.validate(draft),
                () -> engine.handle(config, pathVariables, toQueryParamsArr(queryParams), rawBody));
    }

    private TryResultDto runWithTrace(Runnable preValidation, Supplier<JsonNode> executor) {
        List<StepTraceDto> hops = TraceCollector.start();
        try {
            if (preValidation != null) {
                preValidation.run();
            }
            JsonNode result = executor.get();
            return new TryResultDto(true, result, null, null, List.copyOf(hops));
        } catch (RuntimeException e) {
            String errorCode = (e instanceof BusinessException be) ? be.getErrorCode() : e.getClass().getSimpleName();
            return new TryResultDto(false, null, errorCode, e.getMessage(), List.copyOf(hops));
        } finally {
            TraceCollector.stop();
        }
    }

    /**
     * Bien 1 draft (chua co id that) thanh input cho engine - KHONG can convert
     * nested type nao (EndpointRequestDto/EndpointResponseDto dung chung
     * List<BackendStepDto>/List<FieldMappingDto>), chi bao id gia ("adhoc-try")
     * + createdAt/updatedAt=null. Step tham chieu Upstream Service qua
     * upstreamServiceId - Upstream Service luon duoc tao/luu RIENG truoc khi
     * khai bao endpoint (Canvas chon tu dropdown co san), nen
     * UpstreamRegistryCache.getById() ben trong engine van tra duoc binh
     * thuong du ban than endpoint chua luu.
     */
    private EndpointResponseDto toAdhocResponseDto(EndpointRequestDto dto) {
        return new EndpointResponseDto("adhoc-try", dto.name(), dto.description(), dto.path(), dto.method(),
                dto.sequential(), dto.outputEncoding(), dto.steps(), dto.mappings(), null, null,
                dto.idempotencyEnabled(), dto.idempotencyTtlSeconds(), dto.parallelExecution(),
                dto.responseCacheEnabled(), dto.responseCacheTtlSeconds());
    }

    private Map<String, String[]> toQueryParamsArr(Map<String, String> queryParams) {
        return queryParams.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> new String[]{e.getValue()}));
    }
}
