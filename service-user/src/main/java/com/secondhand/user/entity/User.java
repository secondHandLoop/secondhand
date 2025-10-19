package com.secondhand.user.entity;

import com.secondhand.common.entity.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends Basic {

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    // store a hash, not the raw password
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;


}
