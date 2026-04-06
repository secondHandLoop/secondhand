package com.secondhand.user.service;

import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.dto.ApiResponse;
import com.secondhand.user.dto.ManagerCreateResponse;
import com.secondhand.user.dto.UserResponseMapper;
import com.secondhand.user.entity.Role;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Creates additional manager accounts after checking the operator's role.
 */
@Service
public class ManagerAddService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ManagerAddService(UserRepository userRepository,
                             BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new manager user and returns the created profile summary.
     */
    public ApiResponse<ManagerCreateResponse> createManager(Long currentUserId, String email, String password, String username) {
        AssertUtil.notNullOrBlank(email, "邮箱不能为空");
        AssertUtil.notNullOrBlank(password, "密码不能为空");
        AssertUtil.notNullOrBlank(username, "用户名不能为空");

        User operator = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("当前用户不存在"));
        AssertUtil.isTrue(operator.getRole() == Role.MANAGER, "只有管理员可以创建管理员账号");

        String normalizedEmail = email.trim();
        AssertUtil.isFalse(userRepository.findByEmail(normalizedEmail).isPresent(), "邮箱已注册");

        long now = Instant.now().getEpochSecond();
        User manager = new User();
        manager.setEmail(normalizedEmail);
        manager.setPassword(passwordEncoder.encode(password));
        manager.setUsername(username.trim());
        manager.setRole(Role.MANAGER);
        manager.setEmailVerifiedAt(now);
        manager.setLastLoginAt(0);

        userRepository.save(manager);

        return ApiResponse.success(UserResponseMapper.toManagerCreate(manager));
    }
}
