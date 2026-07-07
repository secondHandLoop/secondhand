package com.secondhand.common.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * Shared local-development CORS policy for every servlet service.
 *
 * <p>Previously each service shipped an identical {@code WebMvcConfigurer} copy;
 * this single {@code CorsFilter} bean is component-scanned into all services
 * (they scan {@code com.secondhand}) so the allowed-origins list lives in one
 * place. Implemented with {@code spring-web} types only, so it needs no extra
 * dependency on the shared module.
 */
@Configuration
public class LocalDevCorsConfig {

    /**
     * Registers a permissive local-development CORS filter.
     *
     * @return the CORS filter applied to every request
     */
    @Bean
    public CorsFilter localDevCorsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://100.107.78.159:*",
                "capacitor://localhost"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
