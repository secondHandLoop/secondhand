package com.secondhand.common.controller;

import com.secondhand.common.auth.JwtTokenService;
import com.secondhand.common.exception.ApiException;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RequestUserIdResolver {

    private final JwtTokenService jwtTokenService;

    public RequestUserIdResolver(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public Long requireUserId(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            throw new ApiException("Authorization header is required", 401);
        }
        return jwtTokenService.parseUserId(extractBearerToken(authorizationHeader));
    }

    public Long resolveOptionalUserId(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            return null;
        }
        return jwtTokenService.parseUserId(extractBearerToken(authorizationHeader));
    }

    public Long requireManagerUserId(String authorizationHeader) {
        String token = extractBearerToken(requireAuthorizationHeader(authorizationHeader));
        // Verify the signature once and read both the subject and role from the
        // same claims, instead of parsing (and re-verifying) the token twice.
        Claims claims = jwtTokenService.parseClaims(token);
        Object role = claims.get("role");
        if (!"MANAGER".equals(role == null ? null : String.valueOf(role))) {
            throw new ApiException("Manager role required", 403);
        }
        return Long.valueOf(claims.getSubject());
    }

    private String requireAuthorizationHeader(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader)) {
            throw new ApiException("Authorization header is required", 401);
        }
        return authorizationHeader;
    }

    private String extractBearerToken(String authorizationHeader) {
        String trimmed = authorizationHeader.trim();
        if (!trimmed.startsWith("Bearer ")) {
            throw new ApiException("Authorization header must use Bearer token", 401);
        }
        return trimmed.substring("Bearer ".length()).trim();
    }
}
