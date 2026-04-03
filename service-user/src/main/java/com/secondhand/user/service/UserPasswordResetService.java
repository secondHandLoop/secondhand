package com.secondhand.user.service;

import com.secondhand.common.output.ApiResponseHelper;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.entity.EmailVerificationChallenge;
import com.secondhand.user.entity.EmailVerificationPurpose;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserPasswordResetService extends ApiResponseHelper {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserPasswordResetService(UserRepository userRepository,
                                    EmailVerificationService emailVerificationService,
                                    BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.emailVerificationService = emailVerificationService;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, Object> requestCode(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        EmailVerificationChallenge challenge = emailVerificationService.createAndSendChallenge(
                user.getEmail(),
                EmailVerificationPurpose.RESET_PASSWORD,
                "password reset"
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("verificationToken", challenge.getChallengeToken());
        body.put("expiresAt", challenge.getExpiresAt());
        return success(body);
    }

    public Map<String, Object> verifyCode(String verificationToken, String code) {
        EmailVerificationChallenge challenge = emailVerificationService.verifyCode(
                verificationToken,
                code,
                EmailVerificationPurpose.RESET_PASSWORD
        );

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("verificationToken", challenge.getChallengeToken());
        body.put("verified", true);
        return success(body);
    }

    public Map<String, Object> completeReset(String verificationToken, String newPassword) {
        AssertUtil.notNullOrBlank(newPassword, "新密码不能为空");
        AssertUtil.isTrue(newPassword.length() >= 6, "密码至少6位");

        EmailVerificationChallenge challenge = emailVerificationService.requireVerifiedChallenge(
                verificationToken,
                EmailVerificationPurpose.RESET_PASSWORD
        );

        User user = userRepository.findByEmail(challenge.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdateAt(System.currentTimeMillis() / 1000);
        userRepository.save(user);

        emailVerificationService.consume(challenge);
        return success();
    }
}
