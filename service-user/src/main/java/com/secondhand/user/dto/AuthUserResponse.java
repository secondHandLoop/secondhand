package com.secondhand.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.secondhand.user.entity.Role;

@Schema(description = "Authenticated user summary embedded in auth responses.")
public class AuthUserResponse {
    @Schema(description = "User id.", example = "1")
    private Long id;
    @Schema(description = "User email address.", example = "user@example.com")
    private String email;
    @Schema(description = "Display name.", example = "Jeffrey")
    private String username;
    @Schema(description = "User role.")
    private Role role;
    @Schema(description = "Unix timestamp in seconds of the last login.", example = "1711992400")
    private Long lastLoginAt;
    @Schema(description = "Unix timestamp in seconds when the email was verified.", example = "1711988800")
    private Long emailVerifiedAt;
    @Schema(description = "User phone number when available.", example = "1234567890")
    private String phone;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Long getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Long lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public Long getEmailVerifiedAt() { return emailVerifiedAt; }
    public void setEmailVerifiedAt(Long emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
