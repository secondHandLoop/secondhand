package com.secondhand.note.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestHeader;

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

    @Bean
    public org.springdoc.core.customizers.OperationCustomizer authorizationHeaderCustomizer() {
        return (operation, handlerMethod) -> {
            RequestHeader authorizationHeader = java.util.Arrays.stream(handlerMethod.getMethodParameters())
                    .map(parameter -> parameter.getParameterAnnotation(RequestHeader.class))
                    .filter(annotation -> annotation != null && isAuthorizationHeader(annotation))
                    .findFirst()
                    .orElse(null);

            if (authorizationHeader != null) {
                operation.addParametersItem(new HeaderParameter()
                        .name("Authorization")
                        .required(authorizationHeader.required())
                        .description("Optional bearer token, for example `Bearer <token>`.")
                        .example("Bearer eyJhbGciOi..."));
            }
            return operation;
        };
    }

    private boolean isAuthorizationHeader(RequestHeader requestHeader) {
        return "Authorization".equals(requestHeader.value()) || "Authorization".equals(requestHeader.name());
    }
}
