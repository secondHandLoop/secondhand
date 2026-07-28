package com.secondhand.note.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Result of grouping a batch of uploaded photos by physical item, so the bulk
 * create-listing flow can turn each group into one listing draft.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Uploaded photos grouped by physical item for bulk listing creation.")
public class PhotoGroupResponse {

    @Schema(description = "One group per detected item; every uploaded photo index appears in exactly one group.")
    private List<Group> groups;

    public List<Group> getGroups() { return groups; }
    public void setGroups(List<Group> groups) { this.groups = groups; }

    /**
     * A set of photos that show the same physical item, plus suggested listing attributes.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Schema(description = "Photos showing the same physical item.")
    public static class Group {

        @Schema(description = "0-based indices into the uploaded photos array.")
        private List<Integer> indices;

        @Schema(description = "Suggested marketplace title for this item, or null.")
        private String suggestedTitle;

        @Schema(description = "Best-guess top category (matches TopCategory enum), or null.")
        private String category;

        @Schema(description = "Best-guess condition (matches NoteCondition enum), or null.")
        private String condition;

        @Schema(description = "AI-extracted attributes JSON (colors, material, shape, keywords), or null.")
        private String attributes;

        public List<Integer> getIndices() { return indices; }
        public void setIndices(List<Integer> indices) { this.indices = indices; }

        public String getSuggestedTitle() { return suggestedTitle; }
        public void setSuggestedTitle(String suggestedTitle) { this.suggestedTitle = suggestedTitle; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public String getCondition() { return condition; }
        public void setCondition(String condition) { this.condition = condition; }

        public String getAttributes() { return attributes; }
        public void setAttributes(String attributes) { this.attributes = attributes; }
    }
}
