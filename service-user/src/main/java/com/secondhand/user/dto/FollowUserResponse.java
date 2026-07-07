package com.secondhand.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A user shown in a following/followers list.")
public class FollowUserResponse {
    @Schema(description = "User id.", example = "1")
    private Long id;
    @Schema(description = "Display name.", example = "Jeffrey")
    private String username;
    @Schema(description = "Profile photo URL.", example = "https://cdn.example.com/avatar.jpg")
    private String profilePhoto;
    @Schema(description = "Whether the current user follows this user.", example = "true")
    private Boolean isFollowing;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getProfilePhoto() { return profilePhoto; }
    public void setProfilePhoto(String profilePhoto) { this.profilePhoto = profilePhoto; }
    public Boolean getIsFollowing() { return isFollowing; }
    public void setIsFollowing(Boolean isFollowing) { this.isFollowing = isFollowing; }
}
