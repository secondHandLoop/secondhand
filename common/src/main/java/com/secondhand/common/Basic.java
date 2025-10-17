package com.secondhand.common;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;

@MappedSuperclass
@Data
@Accessors(chain = true)
public abstract class Basic {

    // Use UUID as String; Hibernate 6 supports GenerationType.UUID
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    @Column(nullable = false)
    private long createAt = Instant.now().getEpochSecond();

    @Column(nullable = false)
    private long updateAt = Instant.now().getEpochSecond();

    @Column(nullable = false)
    private long top = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.ACTIVE;

    @Column(name = "app_id")
    private String app_id;

    @Column(name = "app_secret")
    private String app_secret;
}
