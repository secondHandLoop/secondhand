package com.secondhand.user.service;

import com.secondhand.common.tool.AssertUtil;
import com.secondhand.user.entity.EmailVerificationChallenge;
import com.secondhand.user.entity.EmailVerificationPurpose;
import com.secondhand.user.repository.EmailVerificationChallengeRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Manages the lifecycle of email verification challenges across auth flows.
 */
@Service
public class EmailVerificationService {

    private final EmailVerificationChallengeRepository challengeRepository;
    private final ResendEmailService resendEmailService;
    private final AuthEmailProperties authEmailProperties;

    public EmailVerificationService(EmailVerificationChallengeRepository challengeRepository,
                                    ResendEmailService resendEmailService,
                                    AuthEmailProperties authEmailProperties) {
        this.challengeRepository = challengeRepository;
        this.resendEmailService = resendEmailService;
        this.authEmailProperties = authEmailProperties;
    }

    /**
     * Creates a fresh verification challenge, stores it, and sends the code by email.
     */
    public EmailVerificationChallenge createAndSendChallenge(String email, EmailVerificationPurpose purpose, String scene) {
        long now = Instant.now().getEpochSecond();
        EmailVerificationChallenge challenge = EmailVerificationChallenge.builder()
                .email(email)
                .code(RandomStringUtils.randomNumeric(6))
                .challengeToken(UUID.randomUUID().toString())
                .expiresAt(now + authEmailProperties.getVerificationCodeExpirationSeconds())
                .purpose(purpose)
                .verifiedAt(0L)
                .consumedAt(0L)
                .build();
        challengeRepository.save(challenge);
        resendEmailService.sendVerificationCode(email, challenge.getCode(), scene);
        return challenge;
    }

    /**
     * Verifies a submitted code and records the successful verification timestamp.
     */
    public EmailVerificationChallenge verifyCode(String challengeToken, String code, EmailVerificationPurpose purpose) {
        AssertUtil.notNullOrBlank(challengeToken, "verificationToken不能为空");
        AssertUtil.notNullOrBlank(code, "验证码不能为空");

        EmailVerificationChallenge challenge = challengeRepository
                .findByChallengeTokenAndPurpose(challengeToken, purpose)
                .orElseThrow(() -> new IllegalArgumentException("验证请求不存在"));

        long now = Instant.now().getEpochSecond();
        AssertUtil.isTrue(challenge.getConsumedAt() == 0L, "验证请求已失效");
        AssertUtil.isTrue(challenge.getExpiresAt() >= now, "验证码已过期");
        AssertUtil.equal(challenge.getCode(), code.trim(), "验证码错误");

        challenge.setVerifiedAt(now);
        challenge.setUpdateAt(now);
        challengeRepository.save(challenge);
        return challenge;
    }

    /**
     * Loads a previously verified challenge that is still valid and unused.
     */
    public EmailVerificationChallenge requireVerifiedChallenge(String challengeToken, EmailVerificationPurpose purpose) {
        EmailVerificationChallenge challenge = challengeRepository
                .findByChallengeTokenAndPurpose(challengeToken, purpose)
                .orElseThrow(() -> new IllegalArgumentException("验证请求不存在"));

        long now = Instant.now().getEpochSecond();
        AssertUtil.isTrue(challenge.getConsumedAt() == 0L, "验证请求已失效");
        AssertUtil.isTrue(challenge.getExpiresAt() >= now, "验证请求已过期");
        AssertUtil.isTrue(challenge.getVerifiedAt() > 0L, "请先完成邮箱验证码校验");
        return challenge;
    }

    /**
     * Marks a challenge as consumed so it cannot be reused.
     */
    public void consume(EmailVerificationChallenge challenge) {
        long now = Instant.now().getEpochSecond();
        challenge.setConsumedAt(now);
        challenge.setUpdateAt(now);
        challengeRepository.save(challenge);
    }
}
