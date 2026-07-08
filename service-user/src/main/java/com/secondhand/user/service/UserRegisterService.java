package com.secondhand.user.service;

import com.secondhand.common.auth.JwtTokenService;
import com.secondhand.common.tool.AssertUtil;
import com.secondhand.common.output.ApiResponse;
import com.secondhand.user.dto.AuthSuccessResponse;
import com.secondhand.user.dto.UserResponseMapper;
import com.secondhand.user.dto.VerificationChallengeResponse;
import com.secondhand.user.dto.VerificationStatusResponse;
import com.secondhand.user.entity.EmailVerificationChallenge;
import com.secondhand.user.entity.EmailVerificationPurpose;
import com.secondhand.user.entity.Role;
import com.secondhand.user.entity.User;
import com.secondhand.user.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Handles registration with email verification and token issuance.
 */
@Service
public class UserRegisterService {

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

    /**
     * Starts registration by sending a verification code to an unused email.
     */
    public ApiResponse<VerificationChallengeResponse> requestEmailCode(String email) {
        AssertUtil.notNullOrBlank(email, "邮箱不能为空");
        AssertUtil.isFalse(userRepository.findByEmail(email.trim()).isPresent(), "邮箱已注册");

        EmailVerificationChallenge challenge = emailVerificationService.createAndSendChallenge(
                email.trim(),
                EmailVerificationPurpose.REGISTER,
                "registration"
        );

        VerificationChallengeResponse body = new VerificationChallengeResponse();
        body.setVerificationToken(challenge.getChallengeToken());
        body.setExpiresAt(challenge.getExpiresAt());
        return ApiResponse.success(body);
    }

    /**
     * Validates the registration code and returns a verified token state.
     */
    public ApiResponse<VerificationStatusResponse> verifyEmailCode(String verificationToken, String code) {
        EmailVerificationChallenge challenge = emailVerificationService.verifyCode(
                verificationToken,
                code,
                EmailVerificationPurpose.REGISTER
        );

        VerificationStatusResponse body = new VerificationStatusResponse();
        body.setVerificationToken(challenge.getChallengeToken());
        body.setVerified(true);
        body.setExpiresAt(challenge.getExpiresAt());
        return ApiResponse.success(body);
    }

    /**
     * Creates the user account after the email challenge has been verified.
     */
    public ApiResponse<AuthSuccessResponse> completeRegistration(String verificationToken, String password, String username) {
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

        return ApiResponse.success(buildAuthBody(user));
    }

    /**
     * Builds the auth payload shared by registration and login flows.
     */
    private AuthSuccessResponse buildAuthBody(User user) {
        AuthSuccessResponse body = new AuthSuccessResponse();
        body.setAccessToken(jwtTokenService.createAccessToken(user.getId(), user.getEmail(), user.getRole().name()));
        body.setTokenType("Bearer");
        body.setUser(UserResponseMapper.toAuthUser(user));
        return body;
    }
}
