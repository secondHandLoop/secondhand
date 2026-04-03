package com.secondhand.user.entity;

import com.secondhand.common.entity.Base;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "email_verification_challenge")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailVerificationChallenge extends Base {

    private String email;

    private String code;

    private String challengeToken;

    private long expiresAt;

    private long verifiedAt;

    private long consumedAt;

    @Enumerated(EnumType.STRING)
    private EmailVerificationPurpose purpose;
}
