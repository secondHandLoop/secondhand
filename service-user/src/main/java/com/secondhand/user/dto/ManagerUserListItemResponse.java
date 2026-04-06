package com.secondhand.user.dto;

import com.secondhand.common.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import com.secondhand.user.entity.Role;

@Schema(description = "User summary returned in manager list views.")
public class ManagerUserListItemResponse {
    @Schema(description = "User id.", example = "1")
    private Long id;
    @Schema(description = "Email address.", example = "user@example.com")
    private String email;
    @Schema(description = "Display name.", example = "Jeffrey")
    private String username;
    @Schema(description = "Phone number.", example = "1234567890")
    private String phone;
    @Schema(description = "Profile photo URL.", example = "https://cdn.example.com/avatar.jpg")
    private String profilePhoto;
    @Schema(description = "Marketplace score.", example = "100")
    private Long score;
    @Schema(description = "Profile description.", example = "Secondhand marketplace seller")
    private String description;
    @Schema(description = "User role.")
    private Role role;
    @Schema(description = "Account status.")
    private Status status;
    @Schema(description = "Unix timestamp in seconds when the email was verified.", example = "1711988800")
    private Long emailVerifiedAt;
    @Schema(description = "Unix timestamp in seconds of the last login.", example = "1711992400")
    private Long lastLoginAt;
    @Schema(description = "Unix timestamp in seconds when the account was created.", example = "1711980000")
    private Long createAt;
    @Schema(description = "Unix timestamp in seconds when the account was last updated.", example = "1711992400")
    private Long updateAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
    public Long getScore() { return score; }
    public void setScore(Long score) { this.score = score; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Long getEmailVerifiedAt() { return emailVerifiedAt; }
    public void setEmailVerifiedAt(Long emailVerifiedAt) { this.emailVerifiedAt = emailVerifiedAt; }
    public Long getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Long lastLoginAt) { this.lastLoginAt = lastLoginAt; }
    public Long getCreateAt() { return createAt; }
    public void setCreateAt(Long createAt) { this.createAt = createAt; }
    public Long getUpdateAt() { return updateAt; }
    public void setUpdateAt(Long updateAt) { this.updateAt = updateAt; }
}
