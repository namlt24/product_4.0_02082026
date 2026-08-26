package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.dto.EndpointResponseDto;
import com.bccs.gatewaymanager.repository.EndpointConfigRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

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
        log.info("Da nap lai {} endpoint vao cache dinh tuyen trong-process.", fresh.size());
    }

    public List<EndpointResponseDto> all() {
        return compiled;
    }
}
