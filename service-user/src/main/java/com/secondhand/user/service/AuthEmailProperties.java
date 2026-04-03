package com.secondhand.user.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth.email")
public class AuthEmailProperties {
    private long verificationCodeExpirationSeconds = 600;
    private long inactiveLoginThresholdSeconds = 2_592_000;
    private String resendApiKey;
    private String fromEmail = "Acme <onboarding@resend.dev>";

    public long getVerificationCodeExpirationSeconds() {
        return verificationCodeExpirationSeconds;
    }

    public void setVerificationCodeExpirationSeconds(long verificationCodeExpirationSeconds) {
        this.verificationCodeExpirationSeconds = verificationCodeExpirationSeconds;
    }

    public long getInactiveLoginThresholdSeconds() {
        return inactiveLoginThresholdSeconds;
    }

    public void setInactiveLoginThresholdSeconds(long inactiveLoginThresholdSeconds) {
        this.inactiveLoginThresholdSeconds = inactiveLoginThresholdSeconds;
    }

    public String getResendApiKey() {
        return resendApiKey;
    }

    public void setResendApiKey(String resendApiKey) {
        this.resendApiKey = resendApiKey;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }
}
