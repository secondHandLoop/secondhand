package com.secondhand.note.repository;

import com.secondhand.note.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface HistoryRepository extends JpaRepository<History, Long>, JpaSpecificationExecutor<History> {
    Optional<History> findByUserIdAndNoteId(long userId, long noteId);

    List<History> findByUserIdAndNoteIdIn(long userId, List<Long> noteIds);
}
