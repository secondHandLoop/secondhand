package com.secondhand.user.service;

import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.dto.ApiResponse;
import com.secondhand.user.dto.VerificationChallengeResponse;
import com.secondhand.user.dto.VerificationStatusResponse;
import com.secondhand.user.entity.EmailVerificationChallenge;
import com.secondhand.user.entity.EmailVerificationPurpose;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Coordinates the password-reset flow backed by email verification challenges.
 */
@Service
public class UserPasswordResetService {

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

    /**
     * Sends a reset code to the logged-in user's email address.
     */
    public ApiResponse<VerificationChallengeResponse> requestCode(Long currentUserId) {
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        EmailVerificationChallenge challenge = emailVerificationService.createAndSendChallenge(
                user.getEmail(),
                EmailVerificationPurpose.RESET_PASSWORD,
                "password reset"
        );

        VerificationChallengeResponse body = new VerificationChallengeResponse();
        body.setVerificationToken(challenge.getChallengeToken());
        body.setExpiresAt(challenge.getExpiresAt());
        return ApiResponse.success(body);
    }

    /**
     * Verifies the submitted reset code and returns the verified token state.
     */
    public ApiResponse<VerificationStatusResponse> verifyCode(String verificationToken, String code) {
        EmailVerificationChallenge challenge = emailVerificationService.verifyCode(
                verificationToken,
                code,
                EmailVerificationPurpose.RESET_PASSWORD
        );

        VerificationStatusResponse body = new VerificationStatusResponse();
        body.setVerificationToken(challenge.getChallengeToken());
        body.setVerified(true);
        return ApiResponse.success(body);
    }

    /**
     * Replaces the user's password after a verified reset challenge.
     */
    public ApiResponse<Void> completeReset(String verificationToken, String newPassword) {
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
        return ApiResponse.success();
    }
}
