package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cache dinh tuyen trong-process: nap toan bo EndpointConfig tu DB vao bo nho,
 * de DynamicDispatcherController tra cuu (method, path) cho MOI request that -
 * khong query DB tren duong di nong (hot path) cua traffic.
 *
 * Day chinh la diem khac biet lon nhat so voi KrakenD/Gravitee truoc day: KHONG
 * con buoc "Deploy" (ghi file config + restart container) - chi can goi
 * reload() ngay sau khi Luu/Xoa qua Control Plane, endpoint moi co hieu luc
 * gan nhu tuc thi cho lan request tiep theo.
 *
 * Dung TransactionTemplate (KHONG dung @Transactional annotation) vi reload()
 * duoc goi ca tu @PostConstruct (self-invocation ngay trong class nay - Spring
 * AOP proxy-based KHONG intercept duoc self-invocation nen @Transactional se
 * vo hieu, gay LazyInitializationException khi materialize cac collection LAZY
 * cua entity da co du lieu that trong DB luc khoi dong) lan tu ben ngoai
 * (EndpointService sau create/update/delete). TransactionTemplate boc transaction
 * programmatic, hoat dong dung du goi tu dau.
 */
@Slf4j
@Component
public class EndpointRegistryCache {

    private final EndpointConfigRepository endpointConfigRepository;
    private final EndpointMapper endpointMapper;
    private final TransactionTemplate transactionTemplate;

    private volatile List<EndpointResponseDto> compiled = List.of();

    // Router 2 tang: da so endpoint thuc te la path "tinh" (khong co {param}) -
    // tra O(1) qua HashMap thay vi quet tuyen tinh toan bo danh sach nhu truoc
    // (dispatch() cu goi all() roi lap tung phan tu, O(n) moi request). Chi con
    // nhom co {param} (thuong it hon han) moi phai roi qua PathPattern.matches()
    // tuan tu - xem DynamicDispatcherController.dispatch().
    private volatile Map<String, EndpointResponseDto> exactIndex = Map.of();
    private volatile List<EndpointResponseDto> patternEndpoints = List.of();

    public EndpointRegistryCache(EndpointConfigRepository endpointConfigRepository, EndpointMapper endpointMapper,
                                  PlatformTransactionManager transactionManager) {
        this.endpointConfigRepository = endpointConfigRepository;
        this.endpointMapper = endpointMapper;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setReadOnly(true);
    }

    @PostConstruct
    public void init() {
        reload();
    }

    public synchronized void reload() {
        List<EndpointResponseDto> fresh = transactionTemplate.execute(status ->
                endpointConfigRepository.findAll().stream()
                        .map(endpointMapper::toResponseDto)
                        .toList());
        this.compiled = fresh;

        Map<String, EndpointResponseDto> exact = new HashMap<>();
        List<EndpointResponseDto> patterns = new ArrayList<>();
        for (EndpointResponseDto ep : fresh) {
            if (hasPathVariable(ep.path())) {
                patterns.add(ep);
            } else {
                exact.put(exactKey(ep.method().name(), ep.path()), ep);
            }
        }
        this.exactIndex = Map.copyOf(exact);
        this.patternEndpoints = List.copyOf(patterns);

        log.info("Da nap lai {} endpoint vao cache dinh tuyen trong-process ({} path tinh - O(1), {} path co {{param}} - quet tuan tu).",
                fresh.size(), exact.size(), patterns.size());
    }

    public List<EndpointResponseDto> all() {
        return compiled;
    }

    /** Tra O(1) cho path "tinh" (khong co {param}) - da so endpoint thuc te. Null neu khong khop hoac path co {param}. */
    public EndpointResponseDto findExact(String method, String path) {
        return exactIndex.get(exactKey(method, path));
    }

    /** Chi nhom endpoint co {param} trong path - can PathPattern.matches() quet tuan tu (danh sach nay nho hon han "all()"). */
    public List<EndpointResponseDto> patternEndpoints() {
        return patternEndpoints;
    }

    private static boolean hasPathVariable(String path) {
        return path.indexOf('{') >= 0;
    }

    private static String exactKey(String method, String path) {
        return method + " " + path;
    }
}
