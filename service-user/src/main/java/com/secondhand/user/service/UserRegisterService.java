package com.secondhand.user.service;

import com.secondhand.common.auth.JwtTokenService;
import com.secondhand.common.output.ApiResponseHelper;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.entity.EmailVerificationChallenge;
import com.secondhand.user.entity.EmailVerificationPurpose;
import com.secondhand.user.entity.Role;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserRegisterService extends ApiResponseHelper {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public UserRegisterService(UserRepository userRepository,
                               EmailVerificationService emailVerificationService,
                               BCryptPasswordEncoder passwordEncoder,
                               JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.emailVerificationService = emailVerificationService;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    public Map<String, Object> requestEmailCode(String email) {
        AssertUtil.notNullOrBlank(email, "邮箱不能为空");
        AssertUtil.isFalse(userRepository.findByEmail(email.trim()).isPresent(), "邮箱已注册");

        EmailVerificationChallenge challenge = emailVerificationService.createAndSendChallenge(
                email.trim(),
                EmailVerificationPurpose.REGISTER,
                "registration"
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("verificationToken", challenge.getChallengeToken());
        body.put("expiresAt", challenge.getExpiresAt());
        return success(body);
    }

    public Map<String, Object> verifyEmailCode(String verificationToken, String code) {
        EmailVerificationChallenge challenge = emailVerificationService.verifyCode(
                verificationToken,
                code,
                EmailVerificationPurpose.REGISTER
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("verificationToken", challenge.getChallengeToken());
        body.put("verified", true);
        body.put("expiresAt", challenge.getExpiresAt());
        return success(body);
    }

    public Map<String, Object> completeRegistration(String verificationToken, String password, String username) {
        AssertUtil.notNullOrBlank(password, "密码不能为空");
        AssertUtil.notNullOrBlank(username, "用户名不能为空");

        EmailVerificationChallenge challenge = emailVerificationService.requireVerifiedChallenge(
                verificationToken,
                EmailVerificationPurpose.REGISTER
        );

        String email = challenge.getEmail().trim();
        AssertUtil.isFalse(userRepository.findByEmail(email).isPresent(), "邮箱已注册");

        long now = Instant.now().getEpochSecond();
        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setUsername(username.trim());
        user.setRole(Role.CUSTOMER);
        user.setEmailVerifiedAt(now);
        user.setLastLoginAt(now);

        userRepository.save(user);
        emailVerificationService.consume(challenge);

        return success(buildAuthBody(user));
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
