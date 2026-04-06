package com.secondhand.user.dto;

import com.secondhand.user.entity.Address;
import com.secondhand.user.entity.User;
import org.springframework.util.StringUtils;

public final class UserResponseMapper {

    private UserResponseMapper() {
    }

    public static AddressResponse toAddress(Address source) {
        AddressResponse response = new AddressResponse();
        response.setId(source.getId());
        response.setIsDefault(source.getIsDefault());
        response.setUserId(source.getUserId());
        response.setUsername(source.getUsername());
        response.setState(source.getState());
        response.setCity(source.getCity());
        response.setAddress(source.getAddress());
        response.setZipcode(source.getZipcode());
        response.setPhone(source.getPhone());
        response.setStatus(source.getStatus());
        response.setCreateAt(source.getCreateAt());
        response.setUpdateAt(source.getUpdateAt());
        return response;
    }

    public static AuthUserResponse toAuthUser(User source) {
        AuthUserResponse response = new AuthUserResponse();
        response.setId(source.getId());
        response.setEmail(source.getEmail());
        response.setUsername(source.getUsername());
        response.setRole(source.getRole());
        response.setLastLoginAt(source.getLastLoginAt());
        response.setEmailVerifiedAt(source.getEmailVerifiedAt());
        if (StringUtils.hasText(source.getPhone())) {
            response.setPhone(source.getPhone());
        }
        return response;
    }

    public static UserProfileResponse toProfile(User source) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(source.getId());
        response.setEmail(source.getEmail());
        response.setUsername(source.getUsername());
        response.setRole(source.getRole());
        response.setScore(source.getScore());
        response.setLastLoginAt(source.getLastLoginAt());
        response.setEmailVerifiedAt(source.getEmailVerifiedAt());
        response.setCreateAt(source.getCreateAt());
        response.setUpdateAt(source.getUpdateAt());
        if (StringUtils.hasText(source.getPhone())) response.setPhone(source.getPhone());
        if (StringUtils.hasText(source.getProfilePhoto())) response.setProfilePhoto(source.getProfilePhoto());
        if (StringUtils.hasText(source.getDescription())) response.setDescription(source.getDescription());
        return response;
    }

    public static PublicUserProfileResponse toPublicProfile(User source) {
        PublicUserProfileResponse response = new PublicUserProfileResponse();
        response.setId(source.getId());
        response.setUsername(source.getUsername());
        if (StringUtils.hasText(source.getProfilePhoto())) response.setProfilePhoto(source.getProfilePhoto());
        return response;
    }

    public static ManagerUserListItemResponse toManagerUserListItem(User source) {
        ManagerUserListItemResponse response = new ManagerUserListItemResponse();
        response.setId(source.getId());
        response.setEmail(source.getEmail());
        response.setUsername(source.getUsername());
        response.setPhone(source.getPhone());
        response.setProfilePhoto(source.getProfilePhoto());
        response.setScore(source.getScore());
        response.setDescription(source.getDescription());
        response.setRole(source.getRole());
        response.setStatus(source.getStatus());
        response.setEmailVerifiedAt(source.getEmailVerifiedAt());
        response.setLastLoginAt(source.getLastLoginAt());
        response.setCreateAt(source.getCreateAt());
        response.setUpdateAt(source.getUpdateAt());
        return response;
    }

    public static ManagerCreateResponse toManagerCreate(User source) {
        ManagerCreateResponse response = new ManagerCreateResponse();
        response.setId(source.getId());
        response.setEmail(source.getEmail());
        response.setUsername(source.getUsername());
        response.setRole(source.getRole());
        response.setEmailVerifiedAt(source.getEmailVerifiedAt());
        response.setLastLoginAt(source.getLastLoginAt());
        return response;
    }
}
