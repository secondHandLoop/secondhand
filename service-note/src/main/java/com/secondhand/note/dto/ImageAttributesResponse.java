package com.secondhand.note.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Attributes extracted from a listing photo by the vision model. Used to
 * auto-fill the create-listing form.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Item attributes identified from a listing photo.")
public class ImageAttributesResponse {

    @Schema(description = "Concise marketplace title, e.g. 'Black Wooden Office Desk'.")
    private String suggestedTitle;

    @Schema(description = "Short name of the item, e.g. 'desk'.")
    private String item;

    @Schema(description = "Best-guess top category (matches TopCategory enum), or null.")
    private String category;

    @Schema(description = "Best-guess condition (matches NoteCondition enum), or null.")
    private String condition;

    @Schema(description = "Dominant colors.")
    private List<String> colors;

    @Schema(description = "Overall shape, e.g. 'rectangular'.")
    private String shape;

    @Schema(description = "Main material, e.g. 'wood'.")
    private String material;

    @Schema(description = "Extra descriptive keywords.")
    private List<String> keywords;

    public String getSuggestedTitle() { return suggestedTitle; }
    public void setSuggestedTitle(String suggestedTitle) { this.suggestedTitle = suggestedTitle; }

    public String getItem() { return item; }
    public void setItem(String item) { this.item = item; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public List<String> getColors() { return colors; }
    public void setColors(List<String> colors) { this.colors = colors; }

    public String getShape() { return shape; }
    public void setShape(String shape) { this.shape = shape; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }
}
