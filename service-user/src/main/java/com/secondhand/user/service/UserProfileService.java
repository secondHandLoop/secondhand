package com.secondhand.user.service;

import com.secondhand.common.output.ApiResponseHelper;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserProfileService extends ApiResponseHelper {

    private final UserRepository userRepository;

    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, Object> service(Long currentUserId) {
        AssertUtil.notNull(currentUserId, "用户未登录");

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", user.getId());
        body.put("email", user.getEmail());
        body.put("username", user.getUsername());
        body.put("role", user.getRole());
        body.put("score", user.getScore());
        body.put("lastLoginAt", user.getLastLoginAt());
        body.put("emailVerifiedAt", user.getEmailVerifiedAt());
        body.put("createAt", user.getCreateAt());
        body.put("updateAt", user.getUpdateAt());

        if (StringUtils.hasText(user.getPhone())) {
            body.put("phone", user.getPhone());
        }
        if (StringUtils.hasText(user.getProfilePhoto())) {
            body.put("profilePhoto", user.getProfilePhoto());
        }
        if (StringUtils.hasText(user.getDescription())) {
            body.put("description", user.getDescription());
        }

        return success(body);
    }

    public Map<String, Object> publicProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", user.getId());
        body.put("username", user.getUsername());
        if (StringUtils.hasText(user.getProfilePhoto())) {
            body.put("profilePhoto", user.getProfilePhoto());
        }
        return success(body);
    }
}
