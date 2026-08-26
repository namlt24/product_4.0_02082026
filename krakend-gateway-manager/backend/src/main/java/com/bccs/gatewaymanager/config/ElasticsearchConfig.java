package com.bccs.gatewaymanager.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean(destroyMethod = "close")
    public RestClient elasticsearchRestClient() {
        return RestClient.builder(new HttpHost(host, port, "http")).build();
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
