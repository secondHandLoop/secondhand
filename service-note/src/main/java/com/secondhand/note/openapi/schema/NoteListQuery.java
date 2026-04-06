package com.secondhand.note.openapi.schema;

import com.secondhand.note.entity.TopCategory;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "NoteListQuery", description = "Query parameters accepted by the note list endpoint.")
public class NoteListQuery {

    @Schema(description = "Page number, starting from 1.", example = "1", defaultValue = "1")
    private Integer page;

    @Schema(description = "Number of items per page.", example = "10", defaultValue = "10")
    private Integer limit;

    @Schema(description = "Keyword matched against username, title, and description.", example = "desk")
    private String keyword;

    @Schema(description = "Filter by top-level category.")
    private TopCategory topCategory;

    @Schema(description = "Filter by whether delivery is available.", example = "true")
    private Boolean delivery;

    @Schema(description = "When true, returns only the current user's active notes.", example = "false")
    private Boolean myNotes;

    @Schema(description = "When true, enables personalized explore ranking. This only applies when keyword, topCategory, delivery, and nearby are not set.", example = "false")
    private Boolean explore;

    @ArraySchema(schema = @Schema(description = "Categories used to personalize explore ranking. Repeat the query parameter to pass multiple values.", implementation = TopCategory.class))
    private List<TopCategory> interestCategories;

    @Schema(description = "When true, sorts by proximity using coordinates or location fields.", example = "false")
    private Boolean nearby;

    @Schema(description = "User latitude used for nearby and explore ranking.", example = "30.2672")
    private Double userLat;

    @Schema(description = "User longitude used for nearby and explore ranking.", example = "-97.7431")
    private Double userLng;

    @Schema(description = "User city fallback used when coordinates are not provided.", example = "Austin")
    private String userCity;

    @Schema(description = "User state fallback used when coordinates are not provided.", example = "Texas")
    private String userState;

    @Schema(description = "User zipcode fallback used when coordinates are not provided.", example = "78701")
    private String userZipcode;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public TopCategory getTopCategory() {
        return topCategory;
    }

    public void setTopCategory(TopCategory topCategory) {
        this.topCategory = topCategory;
    }

    public Boolean getDelivery() {
        return delivery;
    }

    public void setDelivery(Boolean delivery) {
        this.delivery = delivery;
    }

    public Boolean getMyNotes() {
        return myNotes;
    }

    public void setMyNotes(Boolean myNotes) {
        this.myNotes = myNotes;
    }

    public Boolean getExplore() {
        return explore;
    }

    public void setExplore(Boolean explore) {
        this.explore = explore;
    }

    public List<TopCategory> getInterestCategories() {
        return interestCategories;
    }

    public void setInterestCategories(List<TopCategory> interestCategories) {
        this.interestCategories = interestCategories;
    }

    public Boolean getNearby() {
        return nearby;
    }

    public void setNearby(Boolean nearby) {
        this.nearby = nearby;
    }

    public Double getUserLat() {
        return userLat;
    }

    public void setUserLat(Double userLat) {
        this.userLat = userLat;
    }

    public Double getUserLng() {
        return userLng;
    }

    public void setUserLng(Double userLng) {
        this.userLng = userLng;
    }

    public String getUserCity() {
        return userCity;
    }

    public void setUserCity(String userCity) {
        this.userCity = userCity;
    }

    public String getUserState() {
        return userState;
    }

    public void setUserState(String userState) {
        this.userState = userState;
    }

    public String getUserZipcode() {
        return userZipcode;
    }

    public void setUserZipcode(String userZipcode) {
        this.userZipcode = userZipcode;
    }
}
