package com.secondhand.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Verification status returned after validating a code.")
public class VerificationStatusResponse {
    @Schema(description = "Verification token that has now been verified.", example = "5ac0b2d4-1f58-4f11-88d4-0a7d5b48d2b9")
    private String verificationToken;
    @Schema(description = "Whether the verification challenge is now verified.", example = "true")
    private Boolean verified;
    @Schema(description = "Unix timestamp in seconds when the verified challenge expires.", example = "1711996000")
    private Long expiresAt;

    public String getVerificationToken() { return verificationToken; }
    public void setVerificationToken(String verificationToken) { this.verificationToken = verificationToken; }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
}
