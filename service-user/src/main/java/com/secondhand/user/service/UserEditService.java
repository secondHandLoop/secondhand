package com.secondhand.user.service;

import com.secondhand.common.tool.AssertUtil;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * Updates the current user's editable profile fields and sensitive credentials.
 */
@Service
public class UserEditService {

    @Resource
    private UserRepository userRepository;

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Applies profile changes and requires the current password for email/password updates.
     */
    public ApiResponse<Void> editUser(Long currentUserId, User user, String currentPassword) {
        User dbUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        AssertUtil.isTrue(currentUserId.equals(user.getId()), "当前用户无权访问");

        boolean emailChanged = StringUtils.hasText(user.getEmail()) && !user.getEmail().equals(dbUser.getEmail());
        boolean passwordChanged = StringUtils.hasText(user.getPassword());

        if (emailChanged || passwordChanged) {
            AssertUtil.notNullOrBlank(currentPassword, "修改邮箱或密码需要输入当前密码");
            boolean passwordMatch = passwordEncoder != null
                    ? passwordEncoder.matches(currentPassword, dbUser.getPassword())
                    : currentPassword.equals(dbUser.getPassword());
            AssertUtil.isTrue(passwordMatch, "当前密码错误");
        }

        if (emailChanged) {
            Optional<User> emailOwner = userRepository.findByEmail(user.getEmail());
            AssertUtil.isFalse(emailOwner.isPresent() && !emailOwner.get().getId().equals(dbUser.getId()), "邮箱已注册");
            dbUser.setEmail(user.getEmail());
        }

        if (StringUtils.hasText(user.getPhone()) && !user.getPhone().equals(dbUser.getPhone())) {
            Optional<User> phoneOwner = userRepository.findByPhone(user.getPhone());
            AssertUtil.isFalse(phoneOwner.isPresent() && !phoneOwner.get().getId().equals(dbUser.getId()), "手机号已注册");
            dbUser.setPhone(user.getPhone());
        }

        if (passwordChanged) {
            String hashed = passwordEncoder != null ? passwordEncoder.encode(user.getPassword()) : user.getPassword();
            dbUser.setPassword(hashed);
        }

        if (user.getProfilePhoto() != null) {
            dbUser.setProfilePhoto(user.getProfilePhoto());
        }
        if (user.getDescription() != null) {
            dbUser.setDescription(user.getDescription());
        }
        if (user.getUsername() != null) {
            dbUser.setUsername(user.getUsername());
        }

        dbUser.setUpdateAt(System.currentTimeMillis() / 1000);
        userRepository.save(dbUser);

        return ApiResponse.success();
    }
}
