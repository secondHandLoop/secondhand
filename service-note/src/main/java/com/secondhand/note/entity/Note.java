package com.secondhand.note.entity;

import com.secondhand.common.entity.Base;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

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

    private List<String> photos;

    private String state;

    private String city;

    private String address;

    private String zipcode;

    private int price;

    private long score;

    private String comment;

    @Enumerated(EnumType.STRING)
    private NoteStatus noteStatus;

    @Enumerated(EnumType.STRING)
    private TopCategory topCategory;

    private boolean delivery;
}
