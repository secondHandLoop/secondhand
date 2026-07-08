package com.secondhand.user.service;

import com.secondhand.common.tool.AssertUtil;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.user.dto.PublicUserProfileResponse;
import com.secondhand.user.dto.UserProfileResponse;
import com.secondhand.user.dto.UserResponseMapper;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import org.springframework.stereotype.Service;

/**
 * Loads private and public user profile views.
 */
@Service
public class UserProfileService {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns the authenticated user's own profile payload.
     */
    public ApiResponse<UserProfileResponse> getProfile(Long currentUserId) {
        AssertUtil.notNull(currentUserId, "用户未登录");

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        return ApiResponse.success(UserResponseMapper.toProfile(user));
    }

    /**
     * Returns the public profile shown to other users.
     */
    public ApiResponse<PublicUserProfileResponse> getPublicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        return ApiResponse.success(UserResponseMapper.toPublicProfile(user));
    }
}
