package com.viettel.bccs.policy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;

import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class LocalClientConfigProfileTest {

    @Test
    void localProfileUsesDynamicClientConfigWithoutStaticClientFallback() throws Exception {
        PropertySource<?> source = new YamlPropertySourceLoader()
                .load("application-local", new ClassPathResource("application-local.yml"))
                .getFirst();

        assertThat(source.getProperty("bccs.config.client.base-url"))
                .isEqualTo("${BCCS_CONFIG_SERVICE_BASE_URL:http://localhost:8084}");
        assertThat(source.getProperty("bccs.config.client-config.enabled")).isEqualTo(true);
        assertThat(source.getProperty("bccs.config.client-config.startup.load-latest")).isEqualTo(true);
        assertThat(source.getProperty("bccs.config.client-config.startup.fail-fast"))
                .isEqualTo("${BCCS_CLIENT_CONFIG_FAIL_FAST:false}");
        assertThat(source.getProperty("bccs.config.client-config.missing-client.mode")).isEqualTo("use-file-config");
    }
}