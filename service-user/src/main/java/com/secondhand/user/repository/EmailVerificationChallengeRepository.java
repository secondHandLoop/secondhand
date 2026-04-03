package com.secondhand.user.repository;

import com.secondhand.user.entity.EmailVerificationChallenge;
import com.secondhand.user.entity.EmailVerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationChallengeRepository extends JpaRepository<EmailVerificationChallenge, Long> {
    Optional<EmailVerificationChallenge> findByChallengeTokenAndPurpose(String challengeToken, EmailVerificationPurpose purpose);
}
