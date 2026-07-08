package com.secondhand.note.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Note-service OpenAPI metadata. The shared Authorization-header customizer
 * lives in {@code com.secondhand.common.doc.OpenApiSupport}.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI noteServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Secondhand Note Service API")
                        .description("HTTP contracts exposed by the note service for frontend clients.")
                        .version("1.0")
                        .contact(new Contact().name("Jeffrey")));
    }
}
