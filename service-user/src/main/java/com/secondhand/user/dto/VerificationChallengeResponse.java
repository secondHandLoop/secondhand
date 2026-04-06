package com.secondhand.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Verification challenge returned after sending or requesting a verification code.")
public class VerificationChallengeResponse {
    @Schema(description = "Opaque verification token used in follow-up verification requests.", example = "5ac0b2d4-1f58-4f11-88d4-0a7d5b48d2b9")
    private String verificationToken;
    @Schema(description = "Unix timestamp in seconds when the challenge expires.", example = "1711996000")
    private Long expiresAt;
    @Schema(description = "Whether an extra verification step is required before login completes.", example = "true")
    private Boolean verificationRequired;
    @Schema(description = "Email address associated with the challenge when the client needs to display it.", example = "user@example.com")
    private String email;

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
    public Boolean getVerificationRequired() { return verificationRequired; }
    public void setVerificationRequired(Boolean verificationRequired) { this.verificationRequired = verificationRequired; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
