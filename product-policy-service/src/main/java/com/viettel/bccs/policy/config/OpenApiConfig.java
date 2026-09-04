package com.viettel.bccs.policy.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI bccsOpenApi(
            @Value("${spring.application.name:product-policy-service}") String applicationName,
            @Value("${bccs.observability.service-version:dev}") String applicationVersion,
            @Value("${bccs.security.jwt.enabled:true}") boolean jwtEnabled) {

        OpenAPI openApi = new OpenAPI()
                .info(new Info()
                        .title(applicationName + " API")
                        .version(applicationVersion)
                        .description("BCCS service API documentation"));

        if (jwtEnabled) {
            openApi.addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                   .components(new Components()
                           .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                   new SecurityScheme()
                                           .name(SECURITY_SCHEME_NAME)
                                           .type(SecurityScheme.Type.HTTP)
                                           .scheme("bearer")
                                           .bearerFormat("JWT")));
        }

        return openApi;
    }
}
