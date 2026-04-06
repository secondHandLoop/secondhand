package com.secondhand.note.openapi.schema;

import com.secondhand.note.entity.NoteCondition;
import com.secondhand.note.entity.NoteStatus;
import com.secondhand.note.entity.TopCategory;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "NoteListItemResponse", description = "One note item returned in the note list.")
public class NoteListItemResponse {

    @Schema(description = "Note id.", example = "123")
    private Long id;

    @Schema(description = "Owner user id.", example = "88")
    private Long userId;

    @Schema(description = "Username of the note owner.", example = "jeffrey")
    private String username;

    @Schema(description = "Note title.", example = "IKEA desk for sale")
    private String title;

    @Schema(description = "Note description.", example = "Solid wood desk in good condition.")
    private String description;

    @ArraySchema(schema = @Schema(description = "Photo URLs.", example = "https://cdn.example.com/notes/123/photo-1.jpg"))
    private List<String> photos;

    @Schema(description = "State.", example = "Texas")
    private String state;

    @Schema(description = "City.", example = "Austin")
    private String city;

    @Schema(description = "Address.", example = "123 Main St")
    private String address;

    @Schema(description = "Zip code.", example = "78701")
    private String zipcode;

    @Schema(description = "Latitude.", example = "30.2672")
    private Double latitude;

    @Schema(description = "Longitude.", example = "-97.7431")
    private Double longitude;

    @Schema(description = "Price in dollars.", example = "45")
    private Integer price;

    @Schema(description = "Top-level category.")
    private TopCategory topCategory;

    @Schema(description = "Item condition.")
    private NoteCondition noteCondition;

    @Schema(description = "Whether delivery is available.", example = "true")
    private Boolean delivery;

    @Schema(description = "Publish status of the note.")
    private NoteStatus noteStatus;

    @Schema(description = "Unix timestamp in seconds for the last update.", example = "1711988800")
    private Long updateAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void setPhotos(List<String> photos) {
        this.photos = photos;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public TopCategory getTopCategory() {
        return topCategory;
    }

    public void setTopCategory(TopCategory topCategory) {
        this.topCategory = topCategory;
    }

    public NoteCondition getNoteCondition() {
        return noteCondition;
    }

    public void setNoteCondition(NoteCondition noteCondition) {
        this.noteCondition = noteCondition;
    }

    public Boolean getDelivery() {
        return delivery;
    }

    public void setDelivery(Boolean delivery) {
        this.delivery = delivery;
    }

    public NoteStatus getNoteStatus() {
        return noteStatus;
    }

    public void setNoteStatus(NoteStatus noteStatus) {
        this.noteStatus = noteStatus;
    }

    public Long getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(Long updateAt) {
        this.updateAt = updateAt;
    }
}
