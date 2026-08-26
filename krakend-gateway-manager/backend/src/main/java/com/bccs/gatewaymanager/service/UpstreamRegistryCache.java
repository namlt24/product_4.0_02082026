package com.bccs.gatewaymanager.service;

import com.bccs.gatewaymanager.entity.UpstreamService;
import com.bccs.gatewaymanager.repository.UpstreamServiceRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cache trong-process cho UpstreamService (khoa theo id) - dung boi
 * CompositeOrchestratorEngine tren duong di nong (hot path) cua traffic. Nap
 * lai ngay sau create/update/delete qua UpstreamServiceService (xem reload()).
 *
 * Dung TransactionTemplate (khong dung @Transactional annotation) - ly do
 * giong het EndpointRegistryCache: reload() duoc goi tu @PostConstruct
 * (self-invocation, Spring AOP proxy khong intercept duoc, @Transactional se
 * vo hieu va gay loi khi DB da co du lieu that luc khoi dong).
 */
@Slf4j
@Component
public class UpstreamRegistryCache {

    private final UpstreamServiceRepository repository;
    private final TransactionTemplate transactionTemplate;

    private volatile Map<String, UpstreamService> byId = Map.of();

    public UpstreamRegistryCache(UpstreamServiceRepository repository, PlatformTransactionManager transactionManager) {
        this.repository = repository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setReadOnly(true);
    }

    @PostConstruct
    public void init() {
        reload();
    }

    public synchronized void reload() {
        List<UpstreamService> all = transactionTemplate.execute(status -> repository.findAll());
        Map<String, UpstreamService> fresh = new ConcurrentHashMap<>();
        all.forEach(u -> fresh.put(u.getId(), u));
        this.byId = fresh;
        log.info("Da nap lai {} Upstream Service vao cache trong-process.", fresh.size());
    }

    public UpstreamService getById(String id) {
        return byId.get(id);
    }
}
