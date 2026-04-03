package com.secondhand.user.service;

import com.secondhand.common.output.ApiResponseHelper;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.entity.Role;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ManagerAddService extends ApiResponseHelper {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public ManagerAddService(UserRepository userRepository,
                             BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> createManager(Long currentUserId, String email, String password, String username) {
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

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("id", manager.getId());
        body.put("email", manager.getEmail());
        body.put("username", manager.getUsername());
        body.put("role", manager.getRole());
        body.put("emailVerifiedAt", manager.getEmailVerifiedAt());
        body.put("lastLoginAt", manager.getLastLoginAt());
        return success(body);
    }
}
