package com.secondhand.common.entity;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@MappedSuperclass
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Base implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private long createAt = System.currentTimeMillis() / 1000;
    private long updateAt = System.currentTimeMillis() / 1000;
    @Enumerated(EnumType.STRING)
    private Status status = Status.ACTIVE;
    private String app_id;
    @Version
    private int ver;

}
