package com.secondhand.user.service;

import com.secondhand.common.exception.ApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Wraps the Resend email API used to deliver verification codes.
 */
@Service
public class ResendEmailService {

    private final AuthEmailProperties authEmailProperties;
    private final RestClient restClient;

    public ResendEmailService(AuthEmailProperties authEmailProperties) {
        this.authEmailProperties = authEmailProperties;
        // Bound the outbound call so a slow/hung Resend API cannot pin request
        // threads indefinitely and exhaust the Tomcat worker pool.
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(3000);
        requestFactory.setReadTimeout(5000);
        this.restClient = RestClient.builder()
                .baseUrl("https://api.resend.com")
                .requestFactory(requestFactory)
                .build();
    }

    /**
     * Sends a single verification-code email for the requested auth scene.
     */
    public void sendVerificationCode(String toEmail, String code, String scene) {
        if (!StringUtils.hasText(authEmailProperties.getResendApiKey())) {
            throw new ApiException("RESEND_API_KEY is not configured", 500);
        }

        String fromEmail = normalizeFromEmail(authEmailProperties.getFromEmail());
        if (!StringUtils.hasText(fromEmail)) {
            throw new ApiException("AUTH_EMAIL_FROM is not configured", 500);
        }

        String html = "<p>You requested a verification code for 2ndhub " + scene + ".</p>"
                + "<h2>" + code + "</h2>"
                + "<p>The code expires in 10 minutes.</p>"
                + "<p>If you did not request this code, you can ignore this email.</p>";

        restClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + authEmailProperties.getResendApiKey())
                .body(Map.of(
                        "from", fromEmail,
                        "to", List.of(toEmail),
                        "subject", "2ndhub verification code",
                        "html", html
                ))
                .retrieve()
                .toBodilessEntity();
    }

    /**
     * Strips accidental wrapping quotes from configured sender addresses.
     */
    private String normalizeFromEmail(String fromEmail) {
        if (!StringUtils.hasText(fromEmail)) {
            return fromEmail;
        }

        String normalized = fromEmail.trim();
        if ((normalized.startsWith("\"") && normalized.endsWith("\""))
                || (normalized.startsWith("'") && normalized.endsWith("'"))) {
            normalized = normalized.substring(1, normalized.length() - 1).trim();
        }
        return normalized;
    }
}
