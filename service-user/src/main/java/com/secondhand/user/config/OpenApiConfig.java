package com.secondhand.user.config;

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
    public OpenAPI userServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Secondhand User Service API")
                        .description("HTTP contracts exposed by the user service for authentication, profiles, and addresses.")
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
                        .description("Bearer token for the current user, for example `Bearer <token>`.")
                        .example("Bearer eyJhbGciOi..."));
            }
            return operation;
        };
    }

    private boolean isAuthorizationHeader(RequestHeader requestHeader) {
        return "Authorization".equals(requestHeader.value()) || "Authorization".equals(requestHeader.name());
    }
}
