package com.secondhand.user.service;

import com.secondhand.common.auth.JwtTokenService;
import com.secondhand.common.output.ApiResponseHelper;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.entity.EmailVerificationChallenge;
import com.secondhand.user.entity.EmailVerificationPurpose;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserLoginService extends ApiResponseHelper {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final AuthEmailProperties authEmailProperties;
    private final JwtTokenService jwtTokenService;

    public UserLoginService(UserRepository userRepository,
                            BCryptPasswordEncoder passwordEncoder,
                            EmailVerificationService emailVerificationService,
                            AuthEmailProperties authEmailProperties,
                            JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailVerificationService = emailVerificationService;
        this.authEmailProperties = authEmailProperties;
        this.jwtTokenService = jwtTokenService;
    }

    public Map<String, Object> login(String email, String password) {
        AssertUtil.notNullOrBlank(email, "邮箱不能为空");
        AssertUtil.notNullOrBlank(password, "密码不能为空");

        User user = userRepository.findByEmail(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("账号不存在"));

        boolean passwordMatch = passwordEncoder.matches(password, user.getPassword());
        AssertUtil.isTrue(passwordMatch, "密码错误");

        long now = Instant.now().getEpochSecond();
        if (requiresEmailReverification(user, now)) {
            EmailVerificationChallenge challenge = emailVerificationService.createAndSendChallenge(
                    user.getEmail(),
                    EmailVerificationPurpose.LOGIN_REVERIFY,
                    "login"
            );

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("verificationRequired", true);
            body.put("verificationToken", challenge.getChallengeToken());
            body.put("expiresAt", challenge.getExpiresAt());
            body.put("email", user.getEmail());
            return success(body);
        }

        user.setLastLoginAt(now);
        userRepository.save(user);
        return success(buildAuthBody(user));
    }

    public Map<String, Object> verifyLoginEmail(String verificationToken, String code) {
        EmailVerificationChallenge challenge = emailVerificationService.verifyCode(
                verificationToken,
                code,
                EmailVerificationPurpose.LOGIN_REVERIFY
        );

        User user = userRepository.findByEmail(challenge.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("账号不存在"));

        long now = Instant.now().getEpochSecond();
        user.setEmailVerifiedAt(now);
        user.setLastLoginAt(now);
        userRepository.save(user);
        emailVerificationService.consume(challenge);

        return success(buildAuthBody(user));
    }

    private boolean requiresEmailReverification(User user, long now) {
        return user.getLastLoginAt() > 0
                && now - user.getLastLoginAt() >= authEmailProperties.getInactiveLoginThresholdSeconds();
    }

    private Map<String, Object> buildAuthBody(User user) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("accessToken", jwtTokenService.createAccessToken(user.getId(), user.getEmail(), user.getRole().name()));
        body.put("tokenType", "Bearer");
        body.put("user", buildUserBody(user));
        return body;
    }

    private Map<String, Object> buildUserBody(User user) {
        Map<String, Object> userBody = new LinkedHashMap<>();
        userBody.put("id", user.getId());
        userBody.put("email", user.getEmail());
        userBody.put("username", user.getUsername());
        userBody.put("role", user.getRole());
        userBody.put("lastLoginAt", user.getLastLoginAt());
        userBody.put("emailVerifiedAt", user.getEmailVerifiedAt());
        if (StringUtils.hasText(user.getPhone())) {
            userBody.put("phone", user.getPhone());
        }
        return userBody;
    }
}
