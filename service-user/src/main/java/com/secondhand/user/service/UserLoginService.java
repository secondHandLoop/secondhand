package com.secondhand.user.service;

import com.secondhand.common.auth.JwtTokenService;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.dto.ApiResponse;
import com.secondhand.user.dto.AuthSuccessResponse;
import com.secondhand.user.dto.UserResponseMapper;
import com.secondhand.user.dto.VerificationChallengeResponse;
import com.secondhand.user.entity.EmailVerificationChallenge;
import com.secondhand.user.entity.EmailVerificationPurpose;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;

/**
 * Handles password login and optional email re-verification for inactive accounts.
 */
@Service
public class UserLoginService {

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

    /**
     * Authenticates a user and may return a verification challenge instead of a token.
     */
    public ApiResponse<?> login(String email, String password) {
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

            VerificationChallengeResponse body = new VerificationChallengeResponse();
            body.setVerificationRequired(true);
            body.setVerificationToken(challenge.getChallengeToken());
            body.setExpiresAt(challenge.getExpiresAt());
            body.setEmail(user.getEmail());
            return ApiResponse.success(body);
        }

        user.setLastLoginAt(now);
        userRepository.save(user);
        return ApiResponse.success(buildAuthBody(user));
    }

    /**
     * Completes the login flow after a verification code has been confirmed.
     */
    public ApiResponse<AuthSuccessResponse> verifyLoginEmail(String verificationToken, String code) {
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

        return ApiResponse.success(buildAuthBody(user));
    }

    /**
     * Determines whether inactivity requires the user to re-verify their email.
     */
    private boolean requiresEmailReverification(User user, long now) {
        return user.getLastLoginAt() > 0
                && now - user.getLastLoginAt() >= authEmailProperties.getInactiveLoginThresholdSeconds();
    }

    /**
     * Builds the standard auth payload returned after successful login.
     */
    private AuthSuccessResponse buildAuthBody(User user) {
        AuthSuccessResponse body = new AuthSuccessResponse();
        body.setAccessToken(jwtTokenService.createAccessToken(user.getId(), user.getEmail(), user.getRole().name()));
        body.setTokenType("Bearer");
        body.setUser(UserResponseMapper.toAuthUser(user));
        return body;
    }
}
