package com.secondhand.note.entity;

import com.alibaba.fastjson.JSON;
import com.secondhand.common.entity.Base;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.*;

import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "note")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Note extends Base {
    private Long userId;

    private String username;

    private String title;

    private String description;

    @Column(columnDefinition = "TEXT")
    private String photos = "[]";

    private String state;

    private String city;

    private String address;

    private String zipcode;

    private Double latitude;

    private Double longitude;

    private int price;

    private long score;

    private String comment;

    @Enumerated(EnumType.STRING)
    private NoteStatus noteStatus;

    @Enumerated(EnumType.STRING)
    private TopCategory topCategory;

    @Enumerated(EnumType.STRING)
    private NoteCondition noteCondition;

    private boolean delivery;

    /** AI-extracted attributes as JSON (colors, material, shape, keywords); feeds semantic search. */
    @Column(columnDefinition = "TEXT")
    private String attributes;

    @Transient
    public List<String> getPhotoList() {
        if (photos == null || photos.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return JSON.parseArray(photos, String.class);
    }

    public void setPhotoList(List<String> photoList) {
        this.photos = JSON.toJSONString(photoList == null ? Collections.emptyList() : photoList);
    }
}
