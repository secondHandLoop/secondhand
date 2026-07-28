package com.secondhand.note.repository;

import com.secondhand.common.entity.Status;
import com.secondhand.note.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CollectionRepository extends JpaRepository<Collection, Long>, JpaSpecificationExecutor<Collection> {
    Optional<Collection> findByUserIdAndNoteId(long userId, long noteId);

    // list only active, latest updated first
    List<Collection> findByUserIdAndStatusOrderByUpdateAtDesc(long userId, Status status);

    /**
     * Hard-deletes every collection row referencing a note (across all users), so a
     * deleted note leaves no dangling favorites behind.
     *
     * @param noteId the note being deleted
     * @return the number of rows removed
     */
    @Modifying
    @Query("delete from Collection c where c.noteId = :noteId")
    int deleteByNoteId(@Param("noteId") long noteId);

}
