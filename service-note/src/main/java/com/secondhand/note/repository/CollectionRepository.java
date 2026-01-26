package com.secondhand.note.repository;

import com.secondhand.common.entity.Status;
import com.secondhand.note.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long>, JpaSpecificationExecutor<Collection> {
    Optional<Collection> findByUserIdAndNoteId(long userId, long noteId);

    // list only active, latest updated first
    List<Collection> findByUserIdAndStatusOrderByUpdateAtDesc(long userId, Status status);

}
