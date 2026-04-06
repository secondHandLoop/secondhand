package com.secondhand.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.secondhand.user.entity.Role;

@Schema(description = "Manager account summary returned after creation.")
public class ManagerCreateResponse {
    @Schema(description = "Manager user id.", example = "2")
    private Long id;
    @Schema(description = "Manager email address.", example = "manager@example.com")
    private String email;
    @Schema(description = "Manager display name.", example = "AdminUser")
    private String username;
    @Schema(description = "Assigned role.")
    private Role role;
    @Schema(description = "Unix timestamp in seconds when the email was verified.", example = "1711988800")
    private Long emailVerifiedAt;
    @Schema(description = "Unix timestamp in seconds of the last login.", example = "0")
    private Long lastLoginAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Long getEmailVerifiedAt() { return emailVerifiedAt; }
    public void setEmailVerifiedAt(Long emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }
    public Long getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Long lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}
