package com.secondhand.user.entity;
import com.secondhand.common.entity.Basic;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Address extends Basic {

    private Long userId;
    private String username;
    private String state;
    private String city;
    private String address;
    private Integer zipcode;
    private Boolean isDefault;
    private String phone;
}