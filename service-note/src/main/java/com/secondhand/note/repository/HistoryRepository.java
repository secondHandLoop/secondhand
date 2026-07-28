package com.secondhand.note.repository;

import com.secondhand.note.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HistoryRepository extends JpaRepository<History, Long>, JpaSpecificationExecutor<History> {
    Optional<History> findByUserIdAndNoteId(long userId, long noteId);

    List<History> findByUserIdAndNoteIdIn(long userId, List<Long> noteIds);

    /**
     * Hard-deletes every browsing-history row referencing a note (across all users),
     * so a deleted note leaves no dangling history behind.
     *
     * @param noteId the note being deleted
     * @return the number of rows removed
     */
    @Modifying
    @Query("delete from History h where h.noteId = :noteId")
    int deleteByNoteId(@Param("noteId") long noteId);
}
