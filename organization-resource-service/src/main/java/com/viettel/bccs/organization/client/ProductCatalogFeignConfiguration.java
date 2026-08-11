package com.viettel.bccs.organization.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(
        prefix = "bccs.client.feign",
        name = "enabled",
        havingValue = "true"
)
@EnableFeignClients(clients = ProductCatalogFeignClient.class)
public class ProductCatalogFeignConfiguration {
}
