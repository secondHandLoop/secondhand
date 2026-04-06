package com.secondhand.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication payload returned after successful login or registration.")
public class AuthSuccessResponse {
    @Schema(description = "JWT access token.", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;
    @Schema(description = "HTTP authorization scheme.", example = "Bearer")
    private String tokenType;
    @Schema(description = "Authenticated user summary.")
    private AuthUserResponse user;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public AuthUserResponse getUser() { return user; }
    public void setUser(AuthUserResponse user) { this.user = user; }
}
