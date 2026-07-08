package com.secondhand.user.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * User-service OpenAPI metadata. The shared Authorization-header customizer
 * lives in {@code com.secondhand.common.doc.OpenApiSupport}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Secondhand User Service API")
                        .description("HTTP contracts exposed by the user service for authentication, profiles, and addresses.")
                        .version("1.0")
                        .contact(new Contact().name("Jeffrey")));
    }
}
