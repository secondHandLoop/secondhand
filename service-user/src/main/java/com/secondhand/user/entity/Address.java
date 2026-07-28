package com.secondhand.user.entity;
import com.secondhand.common.entity.Base;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address extends Base {

    private Long userId;
    private String username;
    private String state;
    private String city;
    private String address;
    private Integer zipcode;
    private Boolean isDefault;
    private String phone;

    // Geocoded once on save (see AddressAddService/AddressEditService) so distance
    // to a listing can be computed from a saved address, not just live GPS.
    private Double latitude;
    private Double longitude;
}