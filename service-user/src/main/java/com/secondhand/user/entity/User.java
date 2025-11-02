package com.secondhand.user.entity;

import com.secondhand.common.entity.Base;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends Base {

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    private String password;

    private String username;

    private String phone;

    private String profilePhoto;

    private long score;

    private String description;

    @Enumerated(EnumType.STRING)
    private Role role;

}
