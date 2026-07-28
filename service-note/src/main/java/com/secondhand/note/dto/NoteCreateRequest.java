package com.secondhand.note.dto;

import com.secondhand.note.entity.NoteCondition;
import com.secondhand.note.entity.NoteStatus;
import com.secondhand.note.entity.TopCategory;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "Multipart request used to create a note.")
public class NoteCreateRequest {

    @Schema(description = "Owner username displayed on the note.", example = "jeffrey")
    private String username;
    @Schema(description = "Note title.", example = "IKEA desk for sale")
    private String title;
    @Schema(description = "Note description.", example = "Solid wood desk in good condition.")
    private String description;
    @Schema(description = "State.", example = "Texas")
    private String state;
    @Schema(description = "City.", example = "Austin")
    private String city;
    @Schema(description = "Street address.", example = "123 Main St")
    private String address;
    @Schema(description = "Zip code.", example = "78701")
    private String zipcode;
    @Schema(description = "Price in dollars.", example = "45")
    private int price;
    @Schema(description = "Initial publication status.")
    private NoteStatus noteStatus;
    @Schema(description = "Top-level category.")
    private TopCategory topCategory;
    @Schema(description = "Item condition.")
    private NoteCondition noteCondition;
    @Schema(description = "Whether delivery is available.", example = "true")
    private boolean delivery;
    @Schema(description = "AI-extracted attributes JSON (colors, material, shape, keywords).")
    private String attributes;
    @ArraySchema(schema = @Schema(description = "Photos to upload with the note.", type = "string", format = "binary"))
    private List<MultipartFile> photos;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getZipcode() { return zipcode; }
    public void setZipcode(String zipcode) { this.zipcode = zipcode; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public NoteStatus getNoteStatus() { return noteStatus; }
    public void setNoteStatus(NoteStatus noteStatus) { this.noteStatus = noteStatus; }
    public TopCategory getTopCategory() { return topCategory; }
    public void setTopCategory(TopCategory topCategory) { this.topCategory = topCategory; }
    public NoteCondition getNoteCondition() { return noteCondition; }
    public void setNoteCondition(NoteCondition noteCondition) { this.noteCondition = noteCondition; }
    public boolean isDelivery() { return delivery; }
    public void setDelivery(boolean delivery) { this.delivery = delivery; }
    public String getAttributes() { return attributes; }
    public void setAttributes(String attributes) { this.attributes = attributes; }
    public List<MultipartFile> getPhotos() { return photos; }
    public void setPhotos(List<MultipartFile> photos) { this.photos = photos; }
}
