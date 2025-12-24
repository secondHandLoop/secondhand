package com.secondhand.note.entity;

import com.secondhand.common.entity.Base;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "collection")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collection extends Base {
    private long userId;
    private long noteId;
}
