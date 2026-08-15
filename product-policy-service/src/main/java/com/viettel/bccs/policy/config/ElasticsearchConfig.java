package com.viettel.bccs.policy.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Slf4j
@Configuration
@EnableConfigurationProperties(ElasticsearchProperties.class)
@ConditionalOnClass({ElasticsearchClient.class, RestClient.class})
@ConditionalOnProperty(prefix = "bccs.elasticsearch", name = "enabled", havingValue = "true")
public class ElasticsearchConfig {

    @Bean
    @ConditionalOnMissingBean(RestClient.class)
    public RestClient elasticsearchRestClient(ElasticsearchProperties properties) {
        List<String> hosts = properties.getHosts();
        HttpHost[] httpHosts = hosts.stream()
                .map(host -> parseHttpHost(host, properties.getScheme()))
                .toArray(HttpHost[]::new);

        log.info("Elasticsearch: connecting to {} (scheme={})", hosts, properties.getScheme());

        RestClientBuilder builder = RestClient.builder(httpHosts)
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder
                        .setConnectTimeout((int) properties.getConnectTimeout().toMillis())
                        .setSocketTimeout((int) properties.getSocketTimeout().toMillis()));

        if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
            BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword()));
            builder.setHttpClientConfigCallback(httpClientBuilder ->
                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnMissingBean(ElasticsearchTransport.class)
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    @Bean
    @ConditionalOnMissingBean(ElasticsearchClient.class)
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        return new ElasticsearchClient(transport);
    }

    private static HttpHost parseHttpHost(String hostAndPort, String scheme) {
        String[] parts = hostAndPort.trim().split(":", 2);
        String host = parts[0];
        int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
        return new HttpHost(host, port, scheme);
    }
}
