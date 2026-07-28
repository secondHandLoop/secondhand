package com.secondhand.user.dto;

import com.secondhand.common.entity.Status;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Saved address owned by a user.")
public class AddressResponse {
    @Schema(description = "Address id.", example = "101")
    private Long id;
    @Schema(description = "Whether this is the user's default address.", example = "true")
    private Boolean isDefault;
    @Schema(description = "Owner user id.", example = "1")
    private Long userId;
    @Schema(description = "Recipient name.", example = "Jeffrey Ye")
    private String username;
    @Schema(description = "State or province.", example = "Texas")
    private String state;
    @Schema(description = "City.", example = "Austin")
    private String city;
    @Schema(description = "Street address.", example = "123 Main St")
    private String address;
    @Schema(description = "Zip code.", example = "78701")
    private Integer zipcode;
    @Schema(description = "Contact phone number.", example = "1234567890")
    private String phone;
    @Schema(description = "Geocoded latitude of the address, if resolved.", example = "30.2672")
    private Double latitude;
    @Schema(description = "Geocoded longitude of the address, if resolved.", example = "-97.7431")
    private Double longitude;
    @Schema(description = "Record status.")
    private Status status;
    @Schema(description = "Unix timestamp in seconds when the address was created.", example = "1711988800")
    private Long createAt;
    @Schema(description = "Unix timestamp in seconds when the address was last updated.", example = "1711992400")
    private Long updateAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Boolean getIsDefault() { return isDefault; }
    public void setIsDefault(Boolean aDefault) { isDefault = aDefault; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Integer getZipcode() { return zipcode; }
    public void setZipcode(Integer zipcode) { this.zipcode = zipcode; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Long getCreateAt() { return createAt; }
    public void setCreateAt(Long createAt) { this.createAt = createAt; }
    public Long getUpdateAt() { return updateAt; }
    public void setUpdateAt(Long updateAt) { this.updateAt = updateAt; }
}
