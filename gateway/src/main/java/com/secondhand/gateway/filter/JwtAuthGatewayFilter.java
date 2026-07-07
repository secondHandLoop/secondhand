package com.secondhand.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Edge JWT verification for the gateway.
 *
 * <p>Verifies the {@code Authorization: Bearer} token's signature once at the
 * edge and forwards the caller's id as a trusted {@code X-User-Id} header, while
 * stripping any client-supplied {@code X-User-Id} so it cannot be spoofed.
 *
 * <p>The check is <em>validate-if-present</em>: a request with no token passes
 * through untouched so public endpoints (login, register, anonymous browse) keep
 * working, while a request carrying a forged or expired token is rejected here
 * with 401. Per-endpoint "auth required" enforcement still lives in each service;
 * a full deny-by-default policy would need an explicit public-route allowlist and
 * is intentionally out of scope.
 */
@Component
public class JwtAuthGatewayFilter implements GlobalFilter, Ordered {

    private static final String USER_ID_HEADER = "X-User-Id";

    private final SecretKey signingKey;

    public JwtAuthGatewayFilter(
            @Value("${auth.jwt.secret:secondhand-change-me-please-use-a-long-random-secret}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("auth.jwt.secret must be at least 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Always drop any inbound X-User-Id: only a gateway-verified token may set it.
        ServerHttpRequest.Builder mutated = request.mutate()
                .headers(headers -> headers.remove(USER_ID_HEADER));

        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring("Bearer ".length()).trim();
            try {
                Claims claims = Jwts.parser()
                        .verifyWith(signingKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();
                mutated.header(USER_ID_HEADER, claims.getSubject());
            } catch (Exception ex) {
                // A token was supplied but is forged/expired: reject at the edge.
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        }

        return chain.filter(exchange.mutate().request(mutated.build()).build());
    }

    @Override
    public int getOrder() {
        // Run early, before routing.
        return -1;
    }
}
