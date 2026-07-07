package com.secondhand.user.entity;
import com.secondhand.common.entity.Base;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

/**
 * A directed follow relationship: the user {@code followerId} follows the user
 * {@code followeeId}. A soft-deleted row (Status.DEL) represents an unfollow, so
 * a later re-follow reuses the same row instead of creating a duplicate.
 */
@Entity
@Table(name = "follow")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Follow extends Base {

    private Long followerId;
    private Long followeeId;
}
