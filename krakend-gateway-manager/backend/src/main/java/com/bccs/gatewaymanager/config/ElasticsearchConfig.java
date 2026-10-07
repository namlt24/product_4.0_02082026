package com.bccs.gatewaymanager.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PreDestroy;
import org.apache.http.HttpHost;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Client Elasticsearch dung CHUNG voi he thong BCCS (bccs-elasticsearch, xem
 * db-local/docker-compose.yml) - ghi/doc log audit request/hop (xem package
 * "audit"). KHONG dung Spring Data Elasticsearch (qua nang cho nhu cau chi
 * can bulk-index + query DSL don gian nay) - dung truc tiep elasticsearch-java
 * (ImperativeClient), dung version khop CHINH XAC version server dang chay
 * (8.15.3) de giam rui ro lech version.
 *
 * JacksonJsonpMapper dung jackson-databind CLASSIC (com.fasterxml.jackson,
 * Jackson 2.x) - HOAN TOAN TACH BIET voi tools.jackson (Jackson 3) ma app
 * dung cho Spring MVC (xem pom.xml) - khong xung dot classpath, chi la 2 cay
 * Jackson doc lap cung ton tai (da xac nhan qua mvn dependency:tree).
 */
@Configuration
public class ElasticsearchConfig {

    @Value("${gatewaymanager.audit.elasticsearch.host:localhost}")
    private String host;

    @Value("${gatewaymanager.audit.elasticsearch.port:9200}")
    private int port;

    /**
     * Scheduler don connection idle cua pool ES - thay vi de null (khong don),
     * da xac nhan loi that: ES qua WAN lien-DC (10.207.252.244:9202) bi firewall
     * cat ket noi idle am thao sau vai phut, trong khi httpasyncclient 4.x mac
     * dinh GIU connection trong pool vo thoi han -> lan tra cuu log DAU TIEN sau
     * khoang lang rước connection chet -> treo DUNG 30s (socket timeout mac dinh
     * cua RestClient) -> SocketTimeoutException "http-outgoing-N [ACTIVE]"
     * (testlab-c2 2026-10-05, lap lai 3 don 25-37 phut). F5 chay lai thi nhanh vi
     * luc do client da buoc mo ket noi moi. Fix: close connection idle > 60s dinh ky.
     */
    private ScheduledExecutorService idleEvictScheduler;

    @Bean(destroyMethod = "close")
    public RestClient elasticsearchRestClient() throws java.io.IOException {
        // Tu quan ly connection manager (thay vi de RestClient tu tao) de GIU
        // reference cho threadScheduler don idle connection o duoi - httpasyncclient
        // 4.x khong co evictIdleConnections() (API HttpClient 5) va khong export
        // connection manager tu client da build.
        PoolingNHttpClientConnectionManager cm = new PoolingNHttpClientConnectionManager(
                new org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor());
        cm.setMaxTotal(30);
        cm.setDefaultMaxPerRoute(30);

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, "http"));
        builder.setHttpClientConfigCallback(cb -> {
            // KeepAlive 60s: connection idle qua 60s bi coi la het han (expired).
            cb.setKeepAliveStrategy((response, context) -> 60_000L);
            cb.setConnectionManager(cm);
            return cb;
        });
        scheduleIdleConnectionEviction(cm);
        return builder.build();
    }

    /**
     * Don pool connection moi 30s: closeExpiredConnections() + closeIdleConnections(60s).
     * Day la chuan Elastic khuyen cho RestClient (doc "Connection eviction") - kernel
     * Apache HC 4 khong co auto-evict nen phai tu schedule.
     */
    private void scheduleIdleConnectionEviction(PoolingNHttpClientConnectionManager cm) {
        idleEvictScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "gwm-es-idle-evict");
            t.setDaemon(true);
            return t;
        });
        idleEvictScheduler.scheduleWithFixedDelay(() -> {
            try {
                cm.closeExpiredConnections();
                cm.closeIdleConnections(60, TimeUnit.SECONDS);
            } catch (RuntimeException e) {
                // Khong bao gio de loi don dep lam rot scheduler
            }
        }, 30, 30, TimeUnit.SECONDS);
    }

    @PreDestroy
    void stopIdleEvictScheduler() {
        if (idleEvictScheduler != null) {
            idleEvictScheduler.shutdownNow();
        }
    }

    @Bean(destroyMethod = "close")
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        // JacksonJsonpMapper() mac dinh KHONG dang ky module java.time (Instant dung
        // trong RequestAuditEvent/HopAuditEvent) - phai tu tao ObjectMapper rieng +
        // dang ky JavaTimeModule, neu khong moi lan serialize document se loi ngam
        // (da xac nhan qua log that "Loi day audit log... Jackson exception").
        // TAT WRITE_DATES_AS_TIMESTAMPS: mac dinh Jackson ghi Instant thanh SO epoch
        // (vi du 1.78e9), khien Elasticsearch dynamic mapping tu suy "timestamp" la
        // field SO thay vi "date" - pha vo RangeQuery loc theo thoi gian trong
        // LogSearchService (da xac nhan qua document that: timestamp bi luu dang so).
        // Ghi ISO-8601 (chuoi) de ES nhan dung kieu "date" tu dau.
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return new RestClientTransport(restClient, new JacksonJsonpMapper(mapper));
    }

    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }
}
