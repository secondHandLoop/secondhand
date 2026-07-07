package com.secondhand.common.auth;

import com.secondhand.common.exception.ApiException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenService {

    private final String issuer;
    private final long accessTokenExpirationSeconds;
    private final SecretKey signingKey;

    public JwtTokenService(@Value("${auth.jwt.issuer:secondhand}") String issuer,
                           @Value("${auth.jwt.access-token-expiration-seconds:604800}") long accessTokenExpirationSeconds,
                           @Value("${auth.jwt.secret:secondhand-change-me-please-use-a-long-random-secret}") String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("auth.jwt.secret must be at least 32 bytes");
        }
        this.issuer = issuer;
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String email, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenExpirationSeconds)))
                .claim("email", email)
                .claim("role", role)
                .signWith(signingKey)
                .compact();
    }

    public Long parseUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String parseRole(String token) {
        Object role = parseClaims(token).get("role");
        return role == null ? null : String.valueOf(role);
    }

    /**
     * Verifies the token signature once and returns all of its claims, so callers
     * needing both the subject and role do not verify the signature twice.
     *
     * @param token the raw JWT (without the Bearer prefix)
     * @return the verified claims
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            throw new ApiException("Invalid or expired auth token", 401);
        }
    }
}
