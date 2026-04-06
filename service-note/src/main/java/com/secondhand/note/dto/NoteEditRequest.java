package com.secondhand.note.dto;

import com.secondhand.note.entity.NoteCondition;
import com.secondhand.note.entity.NoteStatus;
import com.secondhand.note.entity.TopCategory;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Schema(description = "Multipart request used to update an existing note.")
public class NoteEditRequest {

    @Schema(description = "Updated note title.", example = "IKEA desk for sale")
    private String title;
    @Schema(description = "Updated note description.", example = "Reduced price, still in good condition.")
    private String description;
    @Schema(description = "Updated state.", example = "Texas")
    private String state;
    @Schema(description = "Updated city.", example = "Austin")
    private String city;
    @Schema(description = "Updated street address.", example = "123 Main St")
    private String address;
    @Schema(description = "Updated zip code.", example = "78701")
    private String zipcode;
    @Schema(description = "Updated price in dollars.", example = "40")
    private Integer price;
    @Schema(description = "Updated publication status.")
    private NoteStatus noteStatus;
    @Schema(description = "Updated top-level category.")
    private TopCategory topCategory;
    @Schema(description = "Updated item condition.")
    private NoteCondition noteCondition;
    @Schema(description = "Updated delivery flag.", example = "true")
    private Boolean delivery;
    @ArraySchema(schema = @Schema(description = "Photo URLs to remove from the note.", example = "https://cdn.example.com/notes/123/photo-1.jpg"))
    private List<String> deletePhotos;
    @ArraySchema(schema = @Schema(description = "Additional photos to upload for the note.", type = "string", format = "binary"))
    private List<MultipartFile> newPhotos;

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
    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }
    public NoteStatus getNoteStatus() { return noteStatus; }
    public void setNoteStatus(NoteStatus noteStatus) { this.noteStatus = noteStatus; }
    public TopCategory getTopCategory() { return topCategory; }
    public void setTopCategory(TopCategory topCategory) { this.topCategory = topCategory; }
    public NoteCondition getNoteCondition() { return noteCondition; }
    public void setNoteCondition(NoteCondition noteCondition) { this.noteCondition = noteCondition; }
    public Boolean getDelivery() { return delivery; }
    public void setDelivery(Boolean delivery) { this.delivery = delivery; }
    public List<String> getDeletePhotos() { return deletePhotos; }
    public void setDeletePhotos(List<String> deletePhotos) { this.deletePhotos = deletePhotos; }
    public List<MultipartFile> getNewPhotos() { return newPhotos; }
    public void setNewPhotos(List<MultipartFile> newPhotos) { this.newPhotos = newPhotos; }
}
